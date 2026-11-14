package ro.marketplace.app.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ro.marketplace.application.exception.StripeConfigurationException;
import ro.marketplace.application.exception.StripeProviderException;
import ro.marketplace.application.exception.StripeWebhookVerificationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.DomainException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BAD_REQUEST_TITLE = "Bad Request";
    private static final String CONFLICT_TYPE = "conflict";
    private static final String ERROR_BASE = "https://api.marketplace.ro/errors/";

    @ExceptionHandler(StripeConfigurationException.class)
    public ProblemDetail handleStripeConfiguration(StripeConfigurationException ex) {
        log.error("Stripe payment configuration is incomplete: {}", ex.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Payment Service Unavailable",
                "Stripe test-mode payments are not configured on the server.", "payment-unavailable");
    }

    @ExceptionHandler(StripeProviderException.class)
    public ProblemDetail handleStripeProvider(StripeProviderException ex) {
        log.error("Stripe Checkout request failed", ex);
        return problem(HttpStatus.BAD_GATEWAY, "Payment Provider Error",
                "Stripe could not start the checkout session. Please try again.", "payment-provider");
    }

    @ExceptionHandler(StripeWebhookVerificationException.class)
    public ProblemDetail handleStripeWebhookVerification(StripeWebhookVerificationException ex) {
        log.warn("Rejected Stripe webhook: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid Stripe Webhook",
                "The Stripe webhook signature or payload could not be verified.", "invalid-webhook");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), "not-found");
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflictException(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Resource Conflict", ex.getMessage(), CONFLICT_TYPE);
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage(), "validation");
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Domain Rule Violation", ex.getMessage(), "domain-violation");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE,
                "Validation failed", "validation");

        Map<String, String> invalidParams = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            invalidParams.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("invalidParams", invalidParams);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        // The streaming size cap surfaces while Jackson reads the body; Jackson
        // wraps the runtime exception, so inspect the cause chain before treating
        // the request as malformed JSON.
        if (hasCause(ex, PayloadTooLargeException.class)) {
            return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                    "The request body exceeds the maximum allowed size.", "payload-too-large");
        }
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE,
                "The request body contains malformed JSON or an unsupported field value.", "bad-request");
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (type.isInstance(current)) {
                return true;
            }
        }
        return false;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        log.warn("Data constraint violation: {}", cause);
        return problem(HttpStatus.CONFLICT, "Conflict",
                "Operation violates a data constraint (for example, a duplicate claim).", CONFLICT_TYPE);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Concurrent modification detected: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Conflict",
                "The resource was modified concurrently. Please retry.", CONFLICT_TYPE);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden",
                "The authenticated user is not allowed to perform this operation.", "forbidden");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        String detail = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "A valid bearer token is required to access this resource.";
        return problem(HttpStatus.UNAUTHORIZED, "Authentication Required", detail, "unauthorized");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // Do not echo the rejected value back: it is untrusted input.
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE,
                "Parameter '" + ex.getName() + "' has an invalid value.", "bad-request");
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ProblemDetail handleBinding(ServletRequestBindingException ex) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE,
                "A required request parameter or header is missing or invalid.", "bad-request");
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ProblemDetail handleConstraintViolation(Exception ex) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE,
                "Request parameter validation failed.", "validation");
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleNoResource(Exception ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource Not Found",
                "The requested endpoint does not exist.", "not-found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "The HTTP method is not supported for this endpoint.", "method-not-allowed");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type",
                "The request Content-Type is not supported by this endpoint.", "unsupported-media-type");
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, PayloadTooLargeException.class})
    public ProblemDetail handlePayloadTooLarge(Exception ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                "The request body exceeds the maximum allowed size.", "payload-too-large");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE, ex.getMessage(), "bad-request");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected server error occurred", "internal");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(ERROR_BASE + type));
        return problemDetail;
    }
}
