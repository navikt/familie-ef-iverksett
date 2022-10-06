package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevUtil.opprettFrittståendeBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.temporal.ChronoUnit

internal class FrittståendeBrevRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var frittståendeBrevRepository: FrittståendeBrevRepository

    @Test
    internal fun `lagring og henting av frittstående brev`() {
        val brev = opprettFrittståendeBrev()

        frittståendeBrevRepository.insert(brev)
        val oppdatertBrev = frittståendeBrevRepository.findByIdOrThrow(brev.id)

        assertThat(oppdatertBrev.id).isEqualTo(brev.id)
        assertThat(oppdatertBrev.personIdent).isEqualTo(brev.personIdent)
        assertThat(oppdatertBrev.eksternFagsakId).isEqualTo(brev.eksternFagsakId)
        assertThat(oppdatertBrev.journalførendeEnhet).isEqualTo(brev.journalførendeEnhet)
        assertThat(oppdatertBrev.saksbehandlerIdent).isEqualTo(brev.saksbehandlerIdent)
        assertThat(oppdatertBrev.stønadstype).isEqualTo(brev.stønadstype)
        assertThat(oppdatertBrev.mottakere).isEqualTo(brev.mottakere)
        assertThat(oppdatertBrev.fil).isEqualTo(brev.fil)
        assertThat(oppdatertBrev.brevtype).isEqualTo(brev.brevtype)
        assertThat(oppdatertBrev.journalpostResultat).isEqualTo(brev.journalpostResultat)
        assertThat(oppdatertBrev.distribuerBrevResultat).isEqualTo(brev.distribuerBrevResultat)
        assertThat(oppdatertBrev.opprettetTid).isEqualTo(brev.opprettetTid)
    }

    @Test
    internal fun `skal oppdatere JournalpostResultat`() {
        val brev = opprettFrittståendeBrev()
        frittståendeBrevRepository.insert(brev)
        val journalpostresultat = JournalpostResultatMap(mapOf("ident" to JournalpostResultat("journalpostId")))

        frittståendeBrevRepository.oppdaterJournalpostResultat(brev.id, journalpostresultat)

        val oppdatertResultat = frittståendeBrevRepository.findByIdOrThrow(brev.id).journalpostResultat
        assertThat(oppdatertResultat).isEqualTo(journalpostresultat)
    }

    @Test
    internal fun `skal oppdatere DistribuerBrevResultat`() {
        val brev = opprettFrittståendeBrev()
        frittståendeBrevRepository.insert(brev)
        val distribuerBrevResultat = DistribuerBrevResultatMap(mapOf("1" to DistribuerBrevResultat("bestillingId")))

        frittståendeBrevRepository.oppdaterDistribuerBrevResultat(brev.id, distribuerBrevResultat)

        val oppdatertResultat = frittståendeBrevRepository.findByIdOrThrow(brev.id).distribuerBrevResultat
        assertThat(oppdatertResultat).isEqualTo(distribuerBrevResultat)
    }
}