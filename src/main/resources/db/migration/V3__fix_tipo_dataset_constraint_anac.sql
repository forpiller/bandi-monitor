DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'bandi') THEN
        EXECUTE 'ALTER TABLE bandi DROP CONSTRAINT IF EXISTS bandi_tipo_dataset_check';
        EXECUTE 'ALTER TABLE bandi ADD CONSTRAINT bandi_tipo_dataset_check CHECK (tipo_dataset IN (''GARE_ASP'', ''BANDI_GARE'', ''ANAC_CIG''))';
    END IF;
END $$;
