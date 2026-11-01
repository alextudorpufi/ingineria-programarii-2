package ro.marketplace.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
