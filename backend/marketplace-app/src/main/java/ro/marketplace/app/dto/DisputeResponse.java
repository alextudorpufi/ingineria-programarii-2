package ro.marketplace.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID leadId,
        String leadTitle,
        UUID proId,
        String proName,
        String reason,
        String details,
        int creditsRequested,
        String status,
        String adminDecisionNote,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt
) {}
