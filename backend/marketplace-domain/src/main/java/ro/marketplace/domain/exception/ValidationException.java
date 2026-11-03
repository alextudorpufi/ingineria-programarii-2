package ro.marketplace.domain.exception;

/**
 * Input is syntactically valid but violates a business/data rule (mapped to HTTP 422).
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
