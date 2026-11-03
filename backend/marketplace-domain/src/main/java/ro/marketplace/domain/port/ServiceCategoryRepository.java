package ro.marketplace.domain.port;

/**
 * Read-side port used to validate that a lead references a real service category.
 */
public interface ServiceCategoryRepository {

    boolean existsById(String id);
}
