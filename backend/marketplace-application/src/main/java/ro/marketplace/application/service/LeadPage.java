package ro.marketplace.application.service;

import ro.marketplace.domain.model.Lead;

import java.util.List;

public record LeadPage(List<Lead> leads, long totalElements, int page, int size) {

    public int totalPages() {
        if (size <= 0 || totalElements <= 0) {
            return 0;
        }
        long pages = (totalElements + size - 1) / size;
        return pages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) pages;
    }
}
