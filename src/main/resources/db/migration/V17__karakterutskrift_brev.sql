create table karakterutskrift_brev
(
    id                       UUID PRIMARY KEY,
    person_ident             VARCHAR,
    oppgave_id               VARCHAR,
    ekstern_fagsak_id        NUMERIC,
    journalforende_enhet     VARCHAR,
    fil                      BYTEA,
    brevtype                 VARCHAR,
    gjeldende_ar             VARCHAR,
    stonad_type              VARCHAR,
    journalpost_id           VARCHAR,
    opprettet_tid            TIMESTAMP(3) DEFAULT LOCALTIMESTAMP
);
