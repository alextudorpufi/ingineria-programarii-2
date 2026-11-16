package ro.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    private UUID id;
    private UUID userId;
    private UUID leadId;
    private WalletTransactionType type;
    private int amount;
    private String description;
    private int balanceAfter;
    private LocalDateTime createdAt;

    /**
     * Enforces the ledger invariants: every balance change must have the sign
     * dictated by its type, and the resulting balance can never be negative.
     */
    public WalletTransaction validate() {
        if (type == null) {
            throw new IllegalArgumentException("Wallet transaction type is required");
        }
        switch (type) {
            case LEAD_PURCHASE -> require(amount < 0, "Lead purchases must debit credits");
            case REFUND, TOP_UP, BONUS -> require(amount > 0, type + " must credit credits");
        }
        require(balanceAfter >= 0, "Balance after a transaction must not be negative");
        require(description != null && !description.isBlank(), "Wallet transaction description is required");
        return this;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
