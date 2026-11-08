package ro.marketplace.app.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Minimal CIDR matcher used to pin the Stripe webhook endpoint to Stripe's
 * published source addresses without adding a dependency.
 */
public final class CidrBlock {

    private final byte[] network;
    private final int prefixLength;

    private CidrBlock(byte[] network, int prefixLength) {
        this.network = network;
        this.prefixLength = prefixLength;
    }

    public static CidrBlock parse(String cidr) {
        if (cidr == null || cidr.isBlank()) {
            throw new IllegalArgumentException("CIDR value must not be blank");
        }
        String value = cidr.trim();
        int separator = value.indexOf('/');
        String addressPart = separator < 0 ? value : value.substring(0, separator);

        InetAddress address;
        try {
            address = InetAddress.getByName(addressPart);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Invalid CIDR address: " + cidr, ex);
        }

        byte[] bytes = address.getAddress();
        int defaultPrefix = bytes.length * 8;
        int prefix = separator < 0 ? defaultPrefix : Integer.parseInt(value.substring(separator + 1));
        if (prefix < 0 || prefix > defaultPrefix) {
            throw new IllegalArgumentException("Invalid CIDR prefix length: " + cidr);
        }

        byte[] masked = Arrays.copyOf(bytes, bytes.length);
        for (int bit = prefix; bit < defaultPrefix; bit++) {
            int index = bit / 8;
            int shift = 7 - (bit % 8);
            masked[index] &= (byte) ~(1 << shift);
        }
        return new CidrBlock(masked, prefix);
    }

    public boolean matches(InetAddress candidate) {
        byte[] candidateBytes = candidate.getAddress();
        if (candidateBytes.length != network.length) {
            return false;
        }
        int fullBytes = prefixLength / 8;
        for (int i = 0; i < fullBytes; i++) {
            if (candidateBytes[i] != network[i]) {
                return false;
            }
        }
        int remainingBits = prefixLength % 8;
        if (remainingBits == 0) {
            return true;
        }
        int mask = 0xFF << (8 - remainingBits);
        return (candidateBytes[fullBytes] & mask) == (network[fullBytes] & mask);
    }
}
