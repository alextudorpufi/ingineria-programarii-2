package ro.marketplace.app.exception;

/**
 * Raised when a request body exceeds the configured maximum size. Kept in the
 * web layer so the filter and the exception handler can share it.
 */
public class PayloadTooLargeException extends RuntimeException {

    public PayloadTooLargeException(String message) {
        super(message);
    }
}
