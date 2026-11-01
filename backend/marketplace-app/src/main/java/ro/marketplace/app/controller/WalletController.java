package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.BalanceResponse;
import ro.marketplace.app.dto.TopUpWalletRequest;
import ro.marketplace.app.dto.WalletTransactionResponse;
import ro.marketplace.app.mapper.WalletDtoMapper;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.application.service.WalletService;
import ro.marketplace.domain.model.WalletTransaction;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = Objects.requireNonNull(walletService, "walletService must not be null");
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID userId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        requireSelfOrAdmin(user, userId);
        return ResponseEntity.ok(new BalanceResponse(userId, walletService.getBalance(userId)));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getUserTransactions(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        requireSelfOrAdmin(user, userId);
        List<WalletTransactionResponse> txs = walletService.getUserTransactions(userId).stream()
                .map(WalletDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(txs);
    }

    @PostMapping("/{userId}/top-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletTransactionResponse> topUpCredits(
            @PathVariable UUID userId,
            @Valid @RequestBody TopUpWalletRequest request) {
        WalletTransaction tx = walletService.topUpCredits(userId, request.credits(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletDtoMapper.toResponse(tx));
    }

    private void requireSelfOrAdmin(AuthenticatedUser user, UUID userId) {
        if (!user.isAdmin() && !user.id().equals(userId)) {
            throw new AccessDeniedException("Wallet belongs to another user");
        }
    }
}
