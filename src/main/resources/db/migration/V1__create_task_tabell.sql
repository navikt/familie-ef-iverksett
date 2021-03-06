CREATE TABLE IF NOT EXISTS task (
    id            BIGSERIAL PRIMARY KEY,
    payload       VARCHAR                                              NOT NULL,
    status        VARCHAR(20)  DEFAULT 'UBEHANDLET'::CHARACTER VARYING NOT NULL,
    versjon       BIGINT       DEFAULT 0,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    type          VARCHAR                                              NOT NULL,
    metadata      VARCHAR,
    trigger_tid   TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    avvikstype    VARCHAR
);

CREATE SEQUENCE IF NOT EXISTS task_seq INCREMENT BY 50;
CREATE UNIQUE INDEX IF NOT EXISTS task_payload_type_idx ON task (payload, TYPE);
CREATE INDEX IF NOT EXISTS task_status_idx ON task (STATUS);

CREATE TABLE IF NOT EXISTS task_logg (
    id            BIGSERIAL PRIMARY KEY,
    task_id       BIGINT       NOT NULL
    CONSTRAINT henvendelse_logg_henvendelse_id_fkey REFERENCES task,
    type          VARCHAR      NOT NULL,
    node          VARCHAR(100) NOT NULL,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    melding       VARCHAR,
    endret_av     VARCHAR(100) DEFAULT 'VL'::CHARACTER VARYING
);


CREATE INDEX IF NOT EXISTS henvendelse_logg_henvendelse_id_idx ON task_logg (task_id);
