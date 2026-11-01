package ro.marketplace.application.audit;

import java.util.Objects;

/**
 * Minimal, dependency-free JSON object writer for structured audit payloads.
 *
 * <p>The application layer must stay framework-free, so this deliberately avoids Jackson.
 * It supports the scalar types used by audit events (strings, numbers, booleans, UUIDs).</p>
 */
public final class AuditDetails {

    private AuditDetails() {
    }

    /**
     * Builds a deterministic JSON object from alternating key/value arguments.
     */
    public static String of(Object... keyValues) {
        Objects.requireNonNull(keyValues, "keyValues must not be null");
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Audit details require alternating keys and values");
        }
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) {
                json.append(',');
            }
            String key = Objects.requireNonNull(keyValues[i], "audit detail key must not be null").toString();
            appendQuoted(json, key);
            json.append(':');
            appendValue(json, keyValues[i + 1]);
        }
        return json.append('}').toString();
    }

    private static void appendValue(StringBuilder json, Object value) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else {
            appendQuoted(json, value.toString());
        }
    }

    private static void appendQuoted(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        json.append('"');
    }
}
