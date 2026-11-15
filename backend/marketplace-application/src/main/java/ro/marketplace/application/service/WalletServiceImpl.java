package ro.marketplace.application.service;

import ro.marketplace.application.audit.AuditDetails;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.domain.port.WalletTransactionRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
            int balanceBefore = user.getCreditsBalance();
            if (!userRepository.addCredits(userId, credits)) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            int balanceAfter = getUser(userId).getCreditsBalance();

            WalletTransaction tx = WalletTransaction.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .type(WalletTransactionType.TOP_UP)
                    .amount(credits)
                    .description(description != null ? description : "Top-up " + credits + " credits")
                    .balanceAfter(balanceAfter)
                    .createdAt(LocalDateTime.now(clock))
                    .build();

            WalletTransaction savedTx = walletTransactionRepository.save(tx);
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
            int balanceBefore = user.getCreditsBalance();
            if (!userRepository.deductCredits(userId, credits)) {
                throw new ConflictException("Insufficient credit balance for user " + userId
                        + ": current " + balanceBefore + ", required " + credits);
            }
            int balanceAfter = getUser(userId).getCreditsBalance();

            WalletTransaction tx = WalletTransaction.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .leadId(leadId)
                    .type(WalletTransactionType.LEAD_PURCHASE)
                    .amount(-credits)
                    .description(description != null ? description : "Purchased lead with " + credits + " credits")
                    .balanceAfter(balanceAfter)
                    .createdAt(LocalDateTime.now(clock))
                    .build();

            WalletTransaction savedTx = walletTransactionRepository.save(tx);
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
            int balanceBefore = user.getCreditsBalance();
            if (!userRepository.addCredits(userId, credits)) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            int balanceAfter = getUser(userId).getCreditsBalance();

            WalletTransaction tx = WalletTransaction.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .leadId(leadId)
                    .type(WalletTransactionType.REFUND)
                    .amount(credits)
                    .description(description != null ? description : "Refund " + credits + " credits")
                    .balanceAfter(balanceAfter)
                    .createdAt(LocalDateTime.now(clock))
                    .build();

            WalletTransaction savedTx = walletTransactionRepository.save(tx);
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
    public List<WalletTransaction> getUserTransactions(UUID userId) {
        return walletTransactionRepository.findByUserId(userId);
    }

    @Override
    public int getBalance(UUID userId) {
        return getUser(userId).getCreditsBalance();
    }

    private void validateAmount(int credits, String label) {
        if (credits <= 0) {
            throw new IllegalArgumentException(label + " must be positive");
        }
        if (credits > MAX_CREDITS_PER_OPERATION) {
            throw new IllegalArgumentException(label + " must not exceed " + MAX_CREDITS_PER_OPERATION);
        }
    }

    private User getUser(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
