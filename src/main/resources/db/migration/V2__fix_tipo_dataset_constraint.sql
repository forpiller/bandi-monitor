-- Rimuove il check constraint su tipo_dataset se la tabella esiste già (no-op su DB fresco).
DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'bandi') THEN
        EXECUTE 'ALTER TABLE bandi DROP CONSTRAINT IF EXISTS bandi_tipo_dataset_check';
    END IF;
END $$;
