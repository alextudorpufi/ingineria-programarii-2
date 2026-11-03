package ro.marketplace.app.dto;

import java.util.List;

public record LeadPageResponse(
        List<LeadResponse> content,
        long totalElements,
        int page,
        int size,
        int totalPages
) {
}
