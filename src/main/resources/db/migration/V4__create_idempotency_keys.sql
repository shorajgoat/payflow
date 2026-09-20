CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    request_hash VARCHAR(128) NOT NULL,
    transaction_reference VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_idempotency_key_user UNIQUE (idempotency_key, user_id)
);