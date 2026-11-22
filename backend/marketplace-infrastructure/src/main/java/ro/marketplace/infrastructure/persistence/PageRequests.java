package ro.marketplace.infrastructure.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Normalises offset paging at the persistence boundary.
 *
 * <p>Application services may already clamp their inputs, but a repository port
 * can be called from anywhere. Enforcing the bound here prevents a single
 * request from asking the database for an unbounded page, while keeping the
 * reported {@code PageResult} size honest about what was actually fetched.</p>
 */
public final class PageRequests {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_LIMIT = 50;

    private PageRequests() {
    }

    public static PageRequest of(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }

    public static int limit(int requested, int fallback) {
        int effective = requested > 0 ? requested : fallback;
        return Math.min(effective, MAX_PAGE_SIZE);
    }
}
