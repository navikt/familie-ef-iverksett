package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FrittståendeBrevService(
    private val frittståendeBrevRepository: FrittståendeBrevRepository,
    private val taskService: TaskService
) {

    fun hentFrittståendeBrev(id: UUID) =
        frittståendeBrevRepository.findByIdOrThrow(id)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterJournalpost(frittståendeBrev: FrittståendeBrev, journalpost: Map<String, String>) {
        frittståendeBrevRepository.update(frittståendeBrev)
    }
}