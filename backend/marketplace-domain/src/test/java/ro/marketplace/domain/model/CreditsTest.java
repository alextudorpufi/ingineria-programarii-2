package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditsTest {

    @Test
    @DisplayName("a balance cannot be negative")
    void negativeCreditsAreRejected() {
        assertThatThrownBy(() -> Credits.of(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("addition is overflow-checked")
    void additionIsOverflowChecked() {
        assertThat(Credits.of(10).plus(Credits.of(5))).isEqualTo(Credits.of(15));
        assertThatThrownBy(() -> Credits.of(Integer.MAX_VALUE).plus(Credits.of(1)))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("subtraction cannot underflow")
    void subtractionCannotUnderflow() {
        assertThat(Credits.of(10).minus(Credits.of(4))).isEqualTo(Credits.of(6));
        assertThatThrownBy(() -> Credits.of(10).minus(Credits.of(11)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("availability comparison")
    void isAtLeast() {
        assertThat(Credits.of(10).isAtLeast(Credits.of(10))).isTrue();
        assertThat(Credits.of(10).isAtLeast(Credits.of(11))).isFalse();
    }
}
