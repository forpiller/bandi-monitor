-- Profili SIAD: gare scolastiche per i 4 bandi attivi dal calendario siadsrl.net
INSERT INTO profili_interesse (nome, keywords, cpv_codes, attivo, data_creazione) VALUES
(
    'DM 38/2026 – Dispositivi e formazione docenti',
    'tablet PC robotica coding dispositivi digitali formazione docenti',
    '30213100, 30213300, 32321200, 39162100',
    TRUE,
    NOW()
),
(
    'D.L. 60/2024 – Infrastrutture sportive e laboratori',
    'attrezzature sportive laboratori palestra infrastrutture scolastiche',
    '37400000, 39160000, 45212200',
    TRUE,
    NOW()
),
(
    'Agenda Sud Fase 2 – Arredi e sicurezza scolastica',
    'arredi scolastici sicurezza inclusione aula ambiente',
    '39160000, 39161000, 45343000',
    TRUE,
    NOW()
),
(
    'Arredi 0-6 anni – Nido e infanzia',
    'arredi nido infanzia spazi educativi materiale didattico',
    '39161000, 39162100',
    TRUE,
    NOW()
);
