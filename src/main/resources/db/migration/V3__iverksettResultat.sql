create table iverksett_resultat
(
    behandling_id               uuid not null primary key,
    tilkjentYtelseForUtbetaling json,
    oppdragResultat             json,
    journalpostResultat         json,
    vedtaksBrevResultat         json

);