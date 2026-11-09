package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.CreateCreditCheckoutRequest;
import ro.marketplace.app.dto.CreditBundleResponse;
import ro.marketplace.app.dto.CreditCheckoutResponse;
import ro.marketplace.app.dto.CreditPurchaseStatusResponse;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.application.service.CreditPaymentService;
import ro.marketplace.domain.model.CreditPurchase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("hasRole('PRO')")
public class CreditPaymentController {

    private final CreditPaymentService creditPaymentService;
    private final AuthService authService;

    public CreditPaymentController(CreditPaymentService creditPaymentService, AuthService authService) {
        this.creditPaymentService = Objects.requireNonNull(creditPaymentService,
                "creditPaymentService must not be null");
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
    }

    @GetMapping("/bundles")
    public List<CreditBundleResponse> getBundles() {
        return creditPaymentService.getBundles().stream().map(CreditBundleResponse::from).toList();
    }

    @PostMapping("/purchases")
    public CreditCheckoutResponse createCheckout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateCreditCheckoutRequest request) {
        AuthenticatedUser user = requireProfessional(jwt);
        CreditPurchase purchase = creditPaymentService.createCheckout(user.id(), request.bundleId(), idempotencyKey);
        return new CreditCheckoutResponse(purchase.id(), purchase.checkoutUrl());
    }

    @GetMapping("/purchases/{purchaseId}")
    public CreditPurchaseStatusResponse getPurchaseStatus(
            @PathVariable UUID purchaseId,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = requireProfessional(jwt);
        return CreditPurchaseStatusResponse.from(creditPaymentService.getPurchase(purchaseId, user.id()));
    }

    private AuthenticatedUser requireProfessional(Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        if (!user.isProfessional()) {
            throw new AccessDeniedException("Only professional accounts can purchase credits");
        }
        // The JWT role claim is not enough: verify the current server-side state so a
        // self-registered, unverified account can never reach the payment flow.
        if (!authService.isVerifiedProfessional(user.id())) {
            throw new AccessDeniedException("The professional account has not been verified by an administrator");
        }
        return user;
    }
}
