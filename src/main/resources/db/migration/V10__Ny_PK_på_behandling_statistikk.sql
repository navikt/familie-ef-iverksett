ALTER TABLE behandling_statistikk
    DROP CONSTRAINT behandling_statistikk_pkey;
ALTER TABLE behandling_statistikk
    ADD id UUID;
UPDATE behandling_statistikk
SET id = CAST(LEFT(CAST(behandling_id AS TEXT), 34) || ASCII(hendelse) AS UUID);
ALTER TABLE behandling_statistikk
    ALTER COLUMN id SET NOT NULL;
ALTER TABLE behandling_statistikk
    RENAME TO behandlingsstatistikk;
ALTER TABLE behandlingsstatistikk
    ADD PRIMARY KEY (id);
