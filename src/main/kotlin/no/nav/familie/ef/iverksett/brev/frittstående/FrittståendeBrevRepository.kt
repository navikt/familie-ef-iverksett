package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface FrittståendeBrevRepository :
    RepositoryInterface<FrittståendeBrev, UUID>, InsertUpdateRepository<FrittståendeBrev> {

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE frittstaende_brev SET journalpost_resultat=:journalpostresultat WHERE id=:id")
    fun oppdaterJournalpostResultat(id: UUID, journalpostresultat: JournalpostResultatMap)

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE frittstaende_brev SET distribuer_brev_resultat=:distribuerBrevResultat WHERE id=:id")
    fun oppdaterDistribuerBrevResultat(id: UUID, distribuerBrevResultat: DistribuerBrevResultatMap)
}
