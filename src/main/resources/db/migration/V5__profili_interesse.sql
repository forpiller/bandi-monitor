CREATE TABLE profili_interesse (
    id               BIGSERIAL PRIMARY KEY,
    nome             VARCHAR(200) NOT NULL,
    keywords         TEXT         NOT NULL,
    cpv_codes        TEXT,
    attivo           BOOLEAN      NOT NULL DEFAULT TRUE,
    data_creazione   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profili_attivo ON profili_interesse (attivo);
