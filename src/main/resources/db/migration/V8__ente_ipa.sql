CREATE TABLE ente_ipa (
    id             BIGSERIAL PRIMARY KEY,
    cf             VARCHAR(20)  NOT NULL,
    denominazione  VARCHAR(500),
    comune         VARCHAR(200),
    provincia      VARCHAR(10),
    regione        VARCHAR(100),
    tipologia_amm  VARCHAR(200),
    CONSTRAINT uk_ente_ipa_cf UNIQUE (cf)
);

CREATE INDEX idx_ente_ipa_cf ON ente_ipa (cf);
