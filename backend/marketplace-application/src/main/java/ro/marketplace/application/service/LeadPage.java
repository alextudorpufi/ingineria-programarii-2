package ro.marketplace.application.service;

import ro.marketplace.domain.model.Lead;

import java.util.List;

public record LeadPage(List<Lead> leads, long totalElements, int page, int size) {

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }
}
