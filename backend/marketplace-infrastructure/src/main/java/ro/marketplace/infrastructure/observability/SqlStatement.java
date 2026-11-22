package ro.marketplace.infrastructure.observability;

public record SqlStatement(String sql, long durationMs) {
}
