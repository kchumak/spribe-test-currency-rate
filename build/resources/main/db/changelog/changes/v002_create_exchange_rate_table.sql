CREATE TABLE IF NOT EXISTS TB_EXCHANGE_RATE
(
    id              BIGSERIAL PRIMARY KEY,
    base_currency   VARCHAR(5)     NOT NULL,
    target_currency VARCHAR(5)     NOT NULL,
    rate            NUMERIC(19, 4) NOT NULL,
    timestamp       TIMESTAMP      NOT NULL
);