package ro.marketplace.infrastructure.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Temporary upper bound for list queries. A future paginated API should replace these
 * fixed slices with client-supplied page/size parameters.
 */
public final class PersistenceLimits {

    public static final int MAX_LIST_RESULTS = 500;

    private PersistenceLimits() {
    }

    public static Pageable firstPage() {
        return PageRequest.of(0, MAX_LIST_RESULTS);
    }
}
