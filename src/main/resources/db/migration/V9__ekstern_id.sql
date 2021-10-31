ALTER TABLE iverksett
    ADD COLUMN ekstern_id BIGINT NOT NULL;

UPDATE iverksett
SET ekstern_id = (data -> 'behandling' ->> 'eksternId')::BIGINT;
