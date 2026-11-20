package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RomanianCountyTest {

    @Test
    @DisplayName("county ids round-trip through the lookup map")
    void fromId() {
        assertThat(RomanianCounty.fromId("RO-IS")).contains(RomanianCounty.IS);
        assertThat(RomanianCounty.fromId("RO-B")).contains(RomanianCounty.B);
        assertThat(RomanianCounty.fromId("RO-XX")).isEmpty();
        assertThat(RomanianCounty.fromId(null)).isEmpty();
    }

    @Test
    @DisplayName("known ids use the canonical RO- prefix")
    void knownIds() {
        assertThat(RomanianCounty.isKnownId("RO-CJ")).isTrue();
        assertThat(RomanianCounty.isKnownId("CJ")).isFalse();
    }
}
