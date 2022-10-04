create table frittstaende_brev
(
    id                      UUID PRIMARY KEY,
    person_ident            VARCHAR,
    ekstern_fagsak_id       NUMERIC,
    journalforende_enhet    VARCHAR,
    saksbehandler_ident     VARCHAR,
    stonadstype             VARCHAR,
    fil                     BYTEA,
    brevtype                VARCHAR,
    journalpost_resulat     JSON,
    distribuer_brev_resulat JSON
);
