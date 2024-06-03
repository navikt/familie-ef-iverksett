ALTER TABLE iverksett
    ADD COLUMN versjon BIGINT DEFAULT 1;
ALTER TABLE iverksett_resultat
    ADD COLUMN versjon BIGINT DEFAULT 1;