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
public class Dispute {
    private UUID id;
    private UUID leadId;
    private String leadTitle;
    private UUID proId;
    /** Snapshot of the professional's display name at dispute creation time. */
    private String proName;
    private String reason; // 'wrong_category', 'unreachable_contact', 'incompatible_budget', 'duplicate_or_fake'
    private String details;
    private int creditsRequested;
    private DisputeStatus status;
    private String adminDecisionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
