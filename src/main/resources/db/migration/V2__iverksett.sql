create table iverksett
(
    behandling_id uuid not null primary key,
    data          json
);

create table brev
(
    behandling_id  uuid references iverksett (behandling_id),
    journalpost_id varchar,
    pdf            bytea
);

