package ro.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.marketplace.domain.exception.ConflictException;

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
    private DisputeReason reason;
    private String details;
    private int creditsRequested;
    private DisputeStatus status;
    private String adminDecisionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    /**
     * Resolves a dispute that is still under analysis. Resolved disputes are
     * immutable so an approval can never be silently reversed.
     */
    public void resolve(boolean approved, String decisionNote, LocalDateTime resolvedAt) {
        if (status != DisputeStatus.IN_ANALIZA) {
            throw new ConflictException("Dispute is already resolved with status: "
                    + (status != null ? status.getDbValue() : "unknown"));
        }
        this.status = approved ? DisputeStatus.APROBAT : DisputeStatus.RESPINS;
        this.adminDecisionNote = decisionNote;
        this.resolvedAt = resolvedAt;
    }
}
