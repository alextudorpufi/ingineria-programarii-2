package ro.marketplace.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CidrBlockTest {

    @Test
    @DisplayName("a /32 block matches only the exact Stripe address")
    void singleAddressBlock() throws Exception {
        CidrBlock block = CidrBlock.parse("3.18.12.63/32");

        assertThat(block.matches(InetAddress.getByName("3.18.12.63"))).isTrue();
        assertThat(block.matches(InetAddress.getByName("3.18.12.64"))).isFalse();
    }

    @Test
    @DisplayName("a range block matches every address inside the prefix")
    void rangeBlock() throws Exception {
        CidrBlock block = CidrBlock.parse("10.0.0.0/8");

        assertThat(block.matches(InetAddress.getByName("10.1.2.3"))).isTrue();
        assertThat(block.matches(InetAddress.getByName("11.1.2.3"))).isFalse();
    }

    @Test
    @DisplayName("IPv4 and IPv6 addresses never match each other")
    void addressFamiliesDoNotMix() throws Exception {
        CidrBlock block = CidrBlock.parse("127.0.0.1/32");

        assertThat(block.matches(InetAddress.getByName("::1"))).isFalse();
    }

    @Test
    @DisplayName("invalid prefix lengths are rejected at startup")
    void invalidPrefixIsRejected() {
        assertThatThrownBy(() -> CidrBlock.parse("10.0.0.0/40"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("an address without a prefix is treated as a single host")
    void missingPrefixDefaultsToHost() throws Exception {
        CidrBlock block = CidrBlock.parse("203.0.113.7");

        assertThat(block.matches(InetAddress.getByName("203.0.113.7"))).isTrue();
        assertThat(block.matches(InetAddress.getByName("203.0.113.8"))).isFalse();
    }
}
