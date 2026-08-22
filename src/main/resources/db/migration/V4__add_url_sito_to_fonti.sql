DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'fonti_monitorate') THEN
        EXECUTE 'ALTER TABLE fonti_monitorate ADD COLUMN IF NOT EXISTS url_sito VARCHAR(500)';
        UPDATE fonti_monitorate SET url_sito = 'https://www.consip.it' WHERE nome = 'Consip';
        UPDATE fonti_monitorate SET url_sito = 'https://www.anticorruzione.it' WHERE nome = 'ANAC';
    END IF;
END $$;
