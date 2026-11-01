package ro.marketplace.application.exception;

public class StripeConfigurationException extends RuntimeException {

    public StripeConfigurationException(String message) {
        super(message);
    }
}
