package ro.marketplace.application.service;

import ro.marketplace.application.audit.AuditDetails;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.domain.port.WalletTransactionRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

public class WalletServiceImpl implements WalletService {

    private static final int MAX_CREDITS_PER_OPERATION = 10_000;

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final AuditService auditService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public WalletServiceImpl(UserRepository userRepository,
                             WalletTransactionRepository walletTransactionRepository,
                             AuditService auditService,
                             UnitOfWork unitOfWork,
                             Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.walletTransactionRepository = Objects.requireNonNull(walletTransactionRepository, "walletTransactionRepository must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public WalletTransaction topUpCredits(UUID userId, int credits, String description) {
        validateAmount(credits, "Credits amount");
        return unitOfWork.executeInTransaction(() -> {
            User user = getUser(userId);
            OptionalInt newBalance = userRepository.addCredits(userId, credits);
            if (newBalance.isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            int balanceAfter = newBalance.getAsInt();
            int balanceBefore = balanceAfter - credits;

            WalletTransaction savedTx = recordLedgerEntry(userId, null, WalletTransactionType.TOP_UP,
                    credits, description != null ? description : "Top-up " + credits + " credits", balanceAfter);
            auditService.recordEvent("User", userId.toString(), AuditAction.CREDIT_TOP_UP, user.getEmail(),
                    AuditDetails.of(
                            "amount", credits,
                            "balanceBefore", balanceBefore,
                            "balanceAfter", balanceAfter,
                            "transactionId", savedTx.getId()));
            return savedTx;
        });
    }

    @Override
    public WalletTransaction deductCredits(UUID userId, int credits, String description, UUID leadId) {
        validateAmount(credits, "Credits to deduct");
        return unitOfWork.executeInTransaction(() -> {
            User user = getUser(userId);
            OptionalInt newBalance = userRepository.deductCredits(userId, credits);
            if (newBalance.isEmpty()) {
                throw new ConflictException("Insufficient credit balance for user " + userId
                        + ": current " + user.getCreditsBalance().asInt() + ", required " + credits);
            }
            int balanceAfter = newBalance.getAsInt();
            int balanceBefore = balanceAfter + credits;

            WalletTransaction savedTx = recordLedgerEntry(userId, leadId, WalletTransactionType.LEAD_PURCHASE,
                    -credits, description != null ? description : "Purchased lead with " + credits + " credits",
                    balanceAfter);
            auditService.recordEvent("User", userId.toString(), AuditAction.CREDIT_DEDUCTION, user.getEmail(),
                    AuditDetails.of(
                            "amount", credits,
                            "balanceBefore", balanceBefore,
                            "balanceAfter", balanceAfter,
                            "leadId", leadId,
                            "transactionId", savedTx.getId()));
            return savedTx;
        });
    }

    @Override
    public WalletTransaction refundCredits(UUID userId, int credits, String description, UUID leadId) {
        return refundCredits(userId, credits, description, leadId, "admin");
    }

    @Override
    public WalletTransaction refundCredits(UUID userId, int credits, String description, UUID leadId, String actor) {
        validateAmount(credits, "Refund credits");
        return unitOfWork.executeInTransaction(() -> {
            User user = getUser(userId);
            OptionalInt newBalance = userRepository.addCredits(userId, credits);
            if (newBalance.isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            int balanceAfter = newBalance.getAsInt();
            int balanceBefore = balanceAfter - credits;

            WalletTransaction savedTx = recordLedgerEntry(userId, leadId, WalletTransactionType.REFUND,
                    credits, description != null ? description : "Refund " + credits + " credits", balanceAfter);
            auditService.recordEvent("User", userId.toString(), AuditAction.REFUND_APPROVED,
                    actor != null && !actor.isBlank() ? actor : "admin",
                    AuditDetails.of(
                            "amount", credits,
                            "balanceBefore", balanceBefore,
                            "balanceAfter", balanceAfter,
                            "leadId", leadId,
                            "transactionId", savedTx.getId()));
            return savedTx;
        });
    }

    @Override
    public WalletTransaction grantBonusCredits(UUID userId, int credits, String description, String actor) {
        validateAmount(credits, "Bonus credits");
        return unitOfWork.executeInTransaction(() -> {
            User user = getUser(userId);
            OptionalInt newBalance = userRepository.addCredits(userId, credits);
            if (newBalance.isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            int balanceAfter = newBalance.getAsInt();
            int balanceBefore = balanceAfter - credits;

            WalletTransaction savedTx = recordLedgerEntry(userId, null, WalletTransactionType.BONUS,
                    credits, description != null ? description : "Bonus " + credits + " credits", balanceAfter);
            auditService.recordEvent("User", userId.toString(), AuditAction.CREDIT_BONUS,
                    actor != null && !actor.isBlank() ? actor : "system",
                    AuditDetails.of(
                            "amount", credits,
                            "balanceBefore", balanceBefore,
                            "balanceAfter", balanceAfter,
                            "transactionId", savedTx.getId()));
            return savedTx;
        });
    }

    @Override
    public PageResult<WalletTransaction> getUserTransactions(UUID userId, int page, int size) {
        Objects.requireNonNull(userId, "userId must not be null");
        return walletTransactionRepository.findPageByUserId(userId, Math.max(page, 0), Math.max(size, 1));
    }

    @Override
    public int getBalance(UUID userId) {
        return getUser(userId).getCreditsBalance().asInt();
    }

    private WalletTransaction recordLedgerEntry(UUID userId, UUID leadId, WalletTransactionType type,
                                                int amount, String description, int balanceAfter) {
        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .leadId(leadId)
                .type(type)
                .amount(amount)
                .description(description)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now(clock))
                .build();
        return walletTransactionRepository.save(transaction.validate());
    }

    private void validateAmount(int credits, String label) {
        if (credits <= 0) {
            throw new ValidationException(label + " must be positive");
        }
        if (credits > MAX_CREDITS_PER_OPERATION) {
            throw new ValidationException(label + " must not exceed " + MAX_CREDITS_PER_OPERATION);
        }
    }

    private User getUser(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
