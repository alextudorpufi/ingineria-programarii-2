package ro.marketplace.domain.exception;

/**
 * Input is syntactically valid but violates a business/data rule (mapped to HTTP 422).
 */
public class ValidationException extends DomainException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
