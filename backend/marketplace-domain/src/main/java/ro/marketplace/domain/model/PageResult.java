package ro.marketplace.domain.model;

import java.util.List;

/**
 * Immutable slice of a larger result set.
 *
 * <p>Repository ports that used to return a plain {@code List} while silently
 * capping the query at 500 rows now return a page so callers can see the total
 * size and decide whether another page is needed.</p>
 */
public record PageResult<T>(List<T> content, long totalElements, int page, int size) {

    public PageResult {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be at least 1");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements must not be negative");
        }
        content = content == null ? List.of() : List.copyOf(content);
    }

    public int totalPages() {
        if (totalElements == 0) {
            return 0;
        }
        long pages = (totalElements + size - 1) / size;
        return pages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) pages;
    }

    public boolean hasNext() {
        return page + 1L < totalPages();
    }
}
