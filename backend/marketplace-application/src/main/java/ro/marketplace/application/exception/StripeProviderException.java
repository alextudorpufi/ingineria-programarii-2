package ro.marketplace.application.exception;

public class StripeProviderException extends RuntimeException {

    public StripeProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
