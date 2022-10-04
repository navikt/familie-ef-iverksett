package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevUtil.opprettFrittståendeBrev
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

internal class JournalførFrittståendeBrevTaskTest {

    private val journalpostClient = mockk<JournalpostClient>()
    private val taskService = mockk<TaskService>()
    private val frittståendeBrevRepository = mockk<FrittståendeBrevRepository>()

    private val service = JournalførFrittståendeBrevTask(journalpostClient, taskService, frittståendeBrevRepository)

    private val arkivSlot = mutableListOf<ArkiverDokumentRequest>()
    private val journalpostResultatSlot = slot<JournalpostResultatMap>()

    @BeforeEach
    internal fun setUp() {
        every { taskService.save(any()) } answers { firstArg() }
        mockArkiverDokument()
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev()
        justRun { frittståendeBrevRepository.oppdaterJournalpostResultat(any(), capture(journalpostResultatSlot)) }
    }

    private fun mockArkiverDokument(medFeil: Boolean = false) {
        every { journalpostClient.arkiverDokument(capture(arkivSlot), any()) } answers {
            val request = firstArg<ArkiverDokumentRequest>()
            val fnr = request.avsenderMottaker?.id ?: request.fnr
            if (medFeil && fnr == "22") {
                error("Feilet")
            }
            ArkiverDokumentResponse("$fnr$fnr", true, null)
        }
    }

    @Test
    internal fun `skal legge inn resultat for alle journalposter`() {
        service.doTask(Task("", UUID.randomUUID().toString()))

        verify(exactly = 2) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 2) { frittståendeBrevRepository.oppdaterJournalpostResultat(any(), any()) }

        val journalpostresultat = journalpostResultatSlot.captured.map
        val entries = journalpostresultat.entries.toList()

        assertThat(entries).hasSize(2)
        assertThat(entries[0].key).isEqualTo("11")
        assertThat(entries[0].value.journalpostId).isEqualTo("1111")

        assertThat(entries[1].key).isEqualTo("22")
        assertThat(entries[1].value.journalpostId).isEqualTo("2222")
    }

    @Test
    internal fun `skal ikke legge inn journalposter som feiler og skal feile tasken`() {
        mockArkiverDokument(medFeil = true)

        assertThatThrownBy {
            service.doTask(Task("", UUID.randomUUID().toString()))
        }.hasMessage("Feilet")

        verify(exactly = 2) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { frittståendeBrevRepository.oppdaterJournalpostResultat(any(), any()) }

        val journalpostresultat = journalpostResultatSlot.captured.map
        val entries = journalpostresultat.entries.toList()

        assertThat(entries).hasSize(1)
        assertThat(entries[0].key).isEqualTo("11")
        assertThat(entries[0].value.journalpostId).isEqualTo("1111")
    }

    @Test
    internal fun `skal ikke arkivere allerede arkiverte brev`() {
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev()
            .copy(journalpostResultat = JournalpostResultatMap(mapOf("11" to JournalpostResultat("tidligereOpprettetJournalpostId"))))

        service.doTask(Task("", UUID.randomUUID().toString()))
        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { frittståendeBrevRepository.oppdaterJournalpostResultat(any(), any()) }

        val journalpostresultat = journalpostResultatSlot.captured.map
        val entries = journalpostresultat.entries.toList()

        // skal kun arkivere for person 22 då 11 allerede er arkivert fra før
        assertThat(arkivSlot).hasSize(1)
        assertThat(arkivSlot[0].fnr).isEqualTo("11")
        assertThat(arkivSlot[0].avsenderMottaker?.id).isEqualTo("22")

        assertThat(entries).hasSize(2)
        assertThat(entries[0].key).isEqualTo("11")
        assertThat(entries[0].value.journalpostId).isEqualTo("tidligereOpprettetJournalpostId")

        //nytt
        assertThat(entries[1].key).isEqualTo("22")
        assertThat(entries[1].value.journalpostId).isEqualTo("2222")
    }

    @Test
    internal fun `skal legge inn avsendermottaker hvis ident ikke er lik personident på frittstående brev`() {
        service.doTask(Task("", UUID.randomUUID().toString()))

        val arkivrequests = arkivSlot

        assertThat(arkivrequests).hasSize(2)
        assertThat(arkivrequests[0].fnr).isEqualTo("11")
        assertThat(arkivrequests[0].avsenderMottaker).isNull()

        assertThat(arkivrequests[1].fnr).isEqualTo("11")
        assertThat(arkivrequests[1].avsenderMottaker?.id).isEqualTo("22")
    }
}