create table iverksett_resultat
(
    behandling_id varchar not null primary key,
    tilkjentYtelseForUtbetaling json,
    oppdragResultat json,
    journalpostResultat json

);