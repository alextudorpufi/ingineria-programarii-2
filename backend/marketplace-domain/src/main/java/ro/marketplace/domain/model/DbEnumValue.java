package ro.marketplace.domain.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * Shared, tolerant lookup for enum values persisted as strings.
 *
 * <p>Persistence historically wrote a mix of canonical database values and
 * enum names. Reading accepts either representation, ignoring case and
 * surrounding spaces, and rejects unknown values explicitly instead of
 * silently defaulting.</p>
 */
final class DbEnumValue {

    private DbEnumValue() {
    }

    static <E extends Enum<E>> E parse(
            String rawValue,
            String label,
            E[] values,
            Function<E, String> databaseValue) {
        Objects.requireNonNull(rawValue, label + " must not be null");
        String normalized = rawValue.trim();
        return Arrays.stream(values)
                .filter(value -> databaseValue.apply(value).equalsIgnoreCase(normalized)
                        || value.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown " + label + ": " + rawValue));
    }
}
