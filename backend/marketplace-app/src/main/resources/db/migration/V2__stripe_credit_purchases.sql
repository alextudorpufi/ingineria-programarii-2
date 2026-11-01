CREATE TABLE credit_purchases (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    bundle_id VARCHAR(50) NOT NULL,
    credits INT NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    stripe_session_id VARCHAR(255) UNIQUE,
    checkout_url VARCHAR(2048),
    wallet_transaction_id UUID UNIQUE REFERENCES wallet_transactions(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uq_credit_purchases_user_idempotency UNIQUE (user_id, idempotency_key),
    CONSTRAINT chk_credit_purchases_credits_positive CHECK (credits > 0),
    CONSTRAINT chk_credit_purchases_amount_positive CHECK (amount_minor > 0),
    CONSTRAINT chk_credit_purchases_status CHECK (status IN ('pending', 'paid', 'failed', 'expired')),
    CONSTRAINT chk_credit_purchases_payment_consistency CHECK (
        (status = 'paid' AND wallet_transaction_id IS NOT NULL)
        OR (status <> 'paid' AND wallet_transaction_id IS NULL)
    )
);

CREATE INDEX idx_credit_purchases_user_created
    ON credit_purchases(user_id, created_at DESC);

CREATE TABLE stripe_webhook_events (
    event_id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    received_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
