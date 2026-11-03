package ro.marketplace.app.mapper;

import ro.marketplace.app.dto.WalletTransactionResponse;
import ro.marketplace.domain.model.WalletTransaction;

public final class WalletDtoMapper {

    private WalletDtoMapper() {
    }

    public static WalletTransactionResponse toResponse(WalletTransaction domain) {
        if (domain == null) return null;
        return new WalletTransactionResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getLeadId(),
                domain.getType() != null ? domain.getType().getDbValue() : null,
                domain.getAmount(),
                domain.getDescription(),
                domain.getBalanceAfter(),
                domain.getCreatedAt()
        );
    }
}
