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
}
