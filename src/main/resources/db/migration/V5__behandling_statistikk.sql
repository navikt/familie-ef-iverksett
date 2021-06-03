create table behandling_statistikk
(
    behandling_id               uuid primary key,
    behandling_dvh              json,
    hendelse                    varchar

);