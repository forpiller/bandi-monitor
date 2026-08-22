-- Inserisce i 4 profili SIAD se non gia' presenti (idempotente).
-- Corrisponde allo stato finale dopo le migration V6 + V7.

INSERT INTO profili_interesse (nome, keywords, cpv_codes, attivo, data_creazione)
SELECT 'DM 38/2026 – Dispositivi e formazione docenti',
       'tablet PC dispositivi digitali robotica coding formazione docenti',
       '30213100, 30213300, 37520000',
       TRUE, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM profili_interesse WHERE nome = 'DM 38/2026 – Dispositivi e formazione docenti'
);

INSERT INTO profili_interesse (nome, keywords, cpv_codes, attivo, data_creazione)
SELECT 'Agenda Sud Fase 2 – Aule interattive e ambienti',
       'aula immersiva monitor interattivo LIM ledwall arredi scolastici BES',
       '39160000, 32322000, 33196200',
       TRUE, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM profili_interesse WHERE nome = 'Agenda Sud Fase 2 – Aule interattive e ambienti'
);

INSERT INTO profili_interesse (nome, keywords, cpv_codes, attivo, data_creazione)
SELECT 'Art. 29 / D.L.60 – Laboratori e infrastrutture sportive',
       'laboratori innovativi istituti tecnici robotica STEM infrastrutture sportive palestra',
       '39160000, 37400000, 30200000',
       TRUE, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM profili_interesse WHERE nome = 'Art. 29 / D.L.60 – Laboratori e infrastrutture sportive'
);

INSERT INTO profili_interesse (nome, keywords, cpv_codes, attivo, data_creazione)
SELECT 'Arredi 0-6 anni – Nido e scuola dell''infanzia',
       'arredi nido infanzia asilo scuola infanzia spazi educativi innovativi',
       '39160000, 39161000',
       TRUE, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM profili_interesse WHERE nome = 'Arredi 0-6 anni – Nido e scuola dell''infanzia'
);
