create table behandling_statistikk
(
    behandling_id               uuid primary key,
    behandlingDVH               json,
    hendelse                    varchar

);