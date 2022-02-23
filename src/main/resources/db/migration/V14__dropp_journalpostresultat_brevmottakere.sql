UPDATE iverksett_resultat
SET vedtaksbrevresultat =
        CONCAT('{', journalpostresultat -> 'journalpostId',
               ':', vedtaksbrevresultat, '}')::JSON
WHERE vedtaksbrevresultat IS NOT NULL
AND journalpostresultat IS NOT NULL;


UPDATE iverksett_resultat
SET journalpostresultat =
        CONCAT('{', i.data -> 'søker' -> 'personIdent',
               ':', journalpostresultat, '}')::JSON
from iverksett i
WHERE journalpostresultat IS NOT NULL
  AND iverksett_resultat.behandling_id = i.behandling_id;

UPDATE iverksett_resultat
SET journalpostresultat = journalpostresultatbrevmottakere
WHERE journalpostresultatbrevmottakere IS NOT NULL;

ALTER TABLE iverksett_resultat
DROP COLUMN journalpostresultatbrevmottakere;
