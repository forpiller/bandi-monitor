-- Aggiorna keywords profili SIAD con dati reali estratti dalle pagine siadsrl.net

UPDATE profili_interesse
SET nome     = 'DM 38/2026 – Dispositivi e formazione docenti',
    keywords = 'tablet PC dispositivi digitali robotica coding formazione docenti',
    cpv_codes = '30213100, 30213300, 37520000'
WHERE nome = 'DM 38/2026 – Dispositivi e formazione docenti';

UPDATE profili_interesse
SET nome     = 'Agenda Sud Fase 2 – Aule interattive e ambienti',
    keywords = 'aula immersiva monitor interattivo LIM ledwall arredi scolastici BES',
    cpv_codes = '39160000, 32322000, 33196200'
WHERE nome = 'Agenda Sud Fase 2 – Arredi e sicurezza scolastica';

UPDATE profili_interesse
SET nome     = 'Art. 29 / D.L.60 – Laboratori e infrastrutture sportive',
    keywords = 'laboratori innovativi istituti tecnici robotica STEM infrastrutture sportive palestra',
    cpv_codes = '39160000, 37400000, 30200000'
WHERE nome = 'D.L. 60/2024 – Infrastrutture sportive e laboratori';

UPDATE profili_interesse
SET nome     = 'Arredi 0-6 anni – Nido e scuola dell''infanzia',
    keywords = 'arredi nido infanzia asilo scuola infanzia spazi educativi innovativi',
    cpv_codes = '39160000, 39161000'
WHERE nome = 'Arredi 0-6 anni – Nido e infanzia';
