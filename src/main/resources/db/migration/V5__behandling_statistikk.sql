create table behandling_statistikk
(
    behandling_id               uuid,
    behandling_dvh              json,
    hendelse                    varchar,
    PRIMARY KEY (behandling_id, hendelse)
);