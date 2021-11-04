ALTER TABLE iverksett
    ADD COLUMN ekstern_id BIGINT;

UPDATE iverksett
SET ekstern_id = (data -> 'behandling' ->> 'eksternId')::BIGINT;

ALTER TABLE iverksett
    ALTER COLUMN ekstern_id SET NOT NULL;

CREATE INDEX ON iverksett (ekstern_id);