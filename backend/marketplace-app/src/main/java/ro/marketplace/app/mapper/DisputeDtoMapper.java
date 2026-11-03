package ro.marketplace.app.mapper;

import ro.marketplace.app.dto.DisputeResponse;
import ro.marketplace.domain.model.Dispute;

public final class DisputeDtoMapper {

    private DisputeDtoMapper() {
    }

    public static DisputeResponse toResponse(Dispute domain) {
        if (domain == null) return null;
        return new DisputeResponse(
                domain.getId(),
                domain.getLeadId(),
                domain.getLeadTitle(),
                domain.getProId(),
                domain.getProName(),
                domain.getReason(),
                domain.getDetails(),
                domain.getCreditsRequested(),
                domain.getStatus() != null ? domain.getStatus().getDbValue() : null,
                domain.getAdminDecisionNote(),
                domain.getResolvedAt(),
                domain.getCreatedAt()
        );
    }
}
