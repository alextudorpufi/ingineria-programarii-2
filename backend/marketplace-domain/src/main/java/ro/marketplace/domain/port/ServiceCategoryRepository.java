package ro.marketplace.domain.port;

import java.util.OptionalInt;

/**
 * Read-side port used to validate and price leads against real service categories.
 */
public interface ServiceCategoryRepository {

    boolean existsById(String id);

    /**
     * Returns the platform-configured lead price for a category. The value is
     * authoritative: clients must never be able to choose what a lead costs.
     */
    OptionalInt findAvgLeadPriceById(String id);
}
