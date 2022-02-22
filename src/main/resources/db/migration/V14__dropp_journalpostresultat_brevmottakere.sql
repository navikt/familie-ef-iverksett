UPDATE iverksett_resultat
SET vedtaksbrevresultat =
        CONCAT('{', journalpostresultat -> 'journalpostId',
               ':', vedtaksbrevresultat, '}')::JSON
WHERE vedtaksbrevresultat IS NOT NULL;

UPDATE iverksett_resultat
SET journalpostresultat =
        CONCAT('{', tilkjentytelseforutbetaling -> 'utbetalingsoppdrag' -> 'aktoer',
               ':', journalpostresultat, '}')::JSON
WHERE journalpostresultat IS NOT NULL
AND tilkjentytelseforutbetaling IS NOT NULL;


UPDATE iverksett_resultat
SET journalpostresultat = journalpostresultatbrevmottakere
WHERE journalpostresultatbrevmottakere IS NOT NULL;

ALTER TABLE iverksett_resultat
DROP COLUMN journalpostresultatbrevmottakere;
