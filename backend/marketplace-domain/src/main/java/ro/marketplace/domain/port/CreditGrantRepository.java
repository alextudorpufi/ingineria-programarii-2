package ro.marketplace.domain.port;

import ro.marketplace.domain.model.CreditGrant;

import java.util.UUID;

public interface CreditGrantRepository {

    CreditGrant save(CreditGrant grant);

    /**
     * Total free credits ever granted to the user, used to enforce the lifetime cap.
     */
    long sumAmountByUserId(UUID userId);
}
