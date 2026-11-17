package ro.marketplace.purchase.example;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Illustrative version of the real lead-claim flow. The production code lives in
 * {@code LeadServiceImpl}, {@code WalletServiceImpl}, and the persistence adapters.
 *
 * <p>What this example demonstrates:</p>
 * <ul>
 *   <li>the transaction is owned by {@code UnitOfWork}, not by a controller;</li>
 *   <li>the slot reservation and credit deduction are guarded in SQL;</li>
 *   <li>the claim and ledger entry are appended, never updated;</li>
 *   <li>timestamps come from the injected UTC clock.</li>
 * </ul>
 *
 * <p>Tables: {@code users.credits_balance}, {@code wallet_transactions},
 * {@code lead_claims} (UNIQUE lead_id, pro_id), and {@code leads}.</p>
 */
public final class LeadPurchaseServiceExample {

    /** Non-negative, overflow-checked balance. */
    public record Credits(int value) {
        public Credits {
            if (value < 0) {
                throw new IllegalArgumentException("Credits must not be negative");
            }
        }

        public Credits minus(Credits other) {
            if (value < other.value) {
                throw new IllegalArgumentException("Insufficient credits");
            }
            return new Credits(value - other.value);
        }
    }

    public record Lead(UUID id, int leadPrice, int maxPros, int currentPros) {
    }

    public record LeadClaim(UUID id, UUID leadId, UUID proId, String proName,
                            LocalDateTime claimedAt) {
    }

    public record WalletTransaction(UUID id, UUID userId, UUID leadId, int amount,
                                    int balanceAfter, LocalDateTime createdAt) {
    }

    public record PurchaseConfirmation(LeadClaim claim, WalletTransaction transaction,
                                       LocalDateTime purchasedAt) {
    }

    public static final class LeadAlreadyClaimedException extends RuntimeException {
        public LeadAlreadyClaimedException(String message) {
            super(message);
        }
    }

    public static final class LeadCapacityExceededException extends RuntimeException {
        public LeadCapacityExceededException(String message) {
            super(message);
        }
    }

    public interface LeadRepository {
        boolean existsClaimByLeadIdAndProId(UUID leadId, UUID proId);

        /** Atomic conditional UPDATE: current_pros < max_pros and status is claimable. */
        boolean reserveSlot(UUID leadId);

        LeadClaim createClaim(LeadClaim claim);

        /** Sets the assigned status when the authoritative counter is full. */
        void markAssignedIfFull(UUID leadId);
    }

    public interface WalletService {
        /** Atomic guarded debit of users.credits_balance; throws on insufficient funds. */
        WalletTransaction deductCredits(UUID userId, int credits, String description, UUID leadId);
    }

    public interface UnitOfWork {
        <T> T executeInTransaction(Supplier<T> work);
    }

    private final LeadRepository leadRepository;
    private final WalletService walletService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public LeadPurchaseServiceExample(LeadRepository leadRepository,
                                      WalletService walletService,
                                      UnitOfWork unitOfWork,
                                      Clock clock) {
        this.leadRepository = Objects.requireNonNull(leadRepository);
        this.walletService = Objects.requireNonNull(walletService);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
        this.clock = Objects.requireNonNull(clock);
    }

    public PurchaseConfirmation purchase(Lead lead, UUID proId, String proName) {
        Objects.requireNonNull(lead, "lead must not be null");
        Objects.requireNonNull(proId, "proId must not be null");
        UUID leadId = lead.id();
        Credits price = new Credits(lead.leadPrice());

        return unitOfWork.executeInTransaction(() -> {
            if (leadRepository.existsClaimByLeadIdAndProId(leadId, proId)) {
                throw new LeadAlreadyClaimedException("Lead already claimed by this professional");
            }

            // Guarded UPDATE ... WHERE current_pros < max_pros. A false result means
            // the lead filled up between the read and the write.
            if (!leadRepository.reserveSlot(leadId)) {
                throw new LeadCapacityExceededException("Lead has no remaining slots");
            }

            WalletTransaction transaction = walletService.deductCredits(
                    proId, price.value(), "Lead purchase", leadId);

            LocalDateTime purchasedAt = LocalDateTime.now(clock);
            LeadClaim claim = leadRepository.createClaim(
                    new LeadClaim(UUID.randomUUID(), leadId, proId, proName, purchasedAt));

            // Only transitions a still-claimable lead; a closed lead stays closed.
            leadRepository.markAssignedIfFull(leadId);

            return new PurchaseConfirmation(claim, transaction, purchasedAt);
        });
    }
}
