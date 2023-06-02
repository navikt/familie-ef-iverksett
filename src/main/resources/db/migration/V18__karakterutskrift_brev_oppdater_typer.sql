ALTER TABLE karakterutskrift_brev ALTER COLUMN gjeldende_ar TYPE DATE USING gjeldende_ar::date;
ALTER TABLE karakterutskrift_brev ALTER COLUMN oppgave_id TYPE NUMERIC USING oppgave_id::numeric

