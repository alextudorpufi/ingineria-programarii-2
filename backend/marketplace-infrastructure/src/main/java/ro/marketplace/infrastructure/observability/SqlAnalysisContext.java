package ro.marketplace.infrastructure.observability;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SqlAnalysisContext {

    private final ThreadLocal<Operation> operation = new ThreadLocal<>();

    public boolean enter(String operationName) {
        Operation current = operation.get();
        if (current != null) {
            current.depth++;
            return false;
        }

        Operation newOperation = new Operation(operationName);
        operation.set(newOperation);
        return true;
    }

    public void exit(boolean outerOperation) {
        if (!outerOperation) {
            Operation current = operation.get();
            if (current != null && current.depth > 0) {
                current.depth--;
            }
            return;
        }

        operation.remove();
    }

    public void record(String sql, long durationMs) {
        Operation current = operation.get();
        if (current != null) {
            current.statements.add(new SqlStatement(sql, durationMs));
        }
    }

    public String operationName() {
        Operation current = operation.get();
        return current == null ? "unknown" : current.name;
    }

    public List<SqlStatement> statements() {
        Operation current = operation.get();
        if (current == null) {
            return List.of();
        }
        return List.copyOf(current.statements);
    }

    public void clear() {
        operation.remove();
    }

    private static final class Operation {
        private final String name;
        private int depth;
        private final List<SqlStatement> statements = new ArrayList<>();

        private Operation(String name) {
            this.name = name;
            this.depth = 1;
        }
    }
}
