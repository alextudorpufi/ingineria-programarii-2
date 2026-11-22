package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTransactionTest {

    private static WalletTransaction transaction(WalletTransactionType type, int amount, int balanceAfter) {
        return WalletTransaction.builder()
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description("entry")
                .build();
    }

    @Test
    @DisplayName("credit transactions must have a positive amount")
    void creditTypesRequirePositiveAmounts() {
        assertThat(transaction(WalletTransactionType.TOP_UP, 10, 10).validate()).isNotNull();
        assertThat(transaction(WalletTransactionType.REFUND, 10, 10).validate()).isNotNull();
        assertThat(transaction(WalletTransactionType.BONUS, 10, 10).validate()).isNotNull();

        assertThatThrownBy(() -> transaction(WalletTransactionType.REFUND, -10, 10).validate())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("lead purchases must debit credits")
    void leadPurchasesRequireNegativeAmounts() {
        assertThat(transaction(WalletTransactionType.LEAD_PURCHASE, -10, 0).validate()).isNotNull();

        assertThatThrownBy(() -> transaction(WalletTransactionType.LEAD_PURCHASE, 10, 20).validate())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the resulting balance can never be negative")
    void balanceAfterMustBeNonNegative() {
        assertThatThrownBy(() -> transaction(WalletTransactionType.TOP_UP, 10, -1).validate())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the transaction type and description are required")
    void typeAndDescriptionAreRequired() {
        assertThatThrownBy(() -> WalletTransaction.builder()
                .amount(10).balanceAfter(10).description("entry").build().validate())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WalletTransaction.builder()
                .type(WalletTransactionType.TOP_UP).amount(10).balanceAfter(10).build().validate())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
