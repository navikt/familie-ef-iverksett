package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.ef.iverksett.brev.frittstående.KarakterInnhentingBrevUtil.opprettBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class JournalførKarakterutskriftBrevTaskTest {
    private val journalpostClient = mockk<JournalpostClient>()
    private val taskService = mockk<TaskService>()
    private val karakterutskriftBrevRepository = mockk<KarakterutskriftBrevRepository>()

    private val brevtypeHoved = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE
    private val brevtypeUtvidet = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_UTVIDET_PERIODE

    private val journalførTask =
        JournalførKarakterutskriftBrevTask(journalpostClient, taskService, karakterutskriftBrevRepository)

    private val arkivSlot = slot<ArkiverDokumentRequest>()
    private val brevSlot = slot<KarakterutskriftBrev>()
    private val journalPostId = UUID.randomUUID().toString()

    @BeforeEach
    internal fun setUp() {
        every {
            journalpostClient.arkiverDokument(capture(arkivSlot), any())
        } returns ArkiverDokumentResponse(journalpostId = journalPostId, ferdigstilt = true)
    }

    @Test
    internal fun `journalfør brev for innhenting av karakterutskrift med hovedperiode`() {
        val brev = opprettBrev(brevtypeHoved)
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns brev
        every { karakterutskriftBrevRepository.update(capture(brevSlot)) } returns brev.copy(journalpostId = journalPostId)

        journalførTask.doTask(Task(JournalførKarakterutskriftBrevTask.TYPE, UUID.randomUUID().toString()))
        val arkiverDokumentRequest = arkivSlot.captured
        val arkivertDokument = arkiverDokumentRequest.hoveddokumentvarianter.first()
        val lagretBrev = brevSlot.captured

        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { karakterutskriftBrevRepository.update(any()) }
        assertThat(lagretBrev).isEqualTo(brev.copy(journalpostId = journalPostId))
        validerAtDokumentRequestInneholderBrevVerdier(arkiverDokumentRequest, arkivertDokument, brev)
        assertThat(arkiverDokumentRequest.eksternReferanseId).isEqualTo("62023INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE")
    }

    @Test
    internal fun `journalfør brev for innhenting av karakterutskrift med utvidet periode`() {
        val brev = opprettBrev(brevtypeUtvidet)
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns brev
        every { karakterutskriftBrevRepository.update(capture(brevSlot)) } returns brev.copy(journalpostId = journalPostId)

        journalførTask.doTask(Task(JournalførKarakterutskriftBrevTask.TYPE, UUID.randomUUID().toString()))
        val arkiverDokumentRequest = arkivSlot.captured
        val arkivertDokument = arkiverDokumentRequest.hoveddokumentvarianter.first()
        val lagretBrev = brevSlot.captured

        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { karakterutskriftBrevRepository.update(any()) }
        assertThat(lagretBrev).isEqualTo(brev.copy(journalpostId = journalPostId))
        validerAtDokumentRequestInneholderBrevVerdier(arkiverDokumentRequest, arkivertDokument, brev)
        assertThat(arkiverDokumentRequest.eksternReferanseId).isEqualTo("62023INNHENTING_AV_KARAKTERUTSKRIFT_UTVIDET_PERIODE")
    }

    private fun validerAtDokumentRequestInneholderBrevVerdier(
        arkiverDokumentRequest: ArkiverDokumentRequest,
        arkivertDokument: Dokument,
        brev: KarakterutskriftBrev,
    ) {
        assertThat(arkiverDokumentRequest.fnr).isEqualTo("12345678910")
        assertThat(arkiverDokumentRequest.forsøkFerdigstill).isTrue
        assertThat(arkivertDokument.dokument).isEqualTo(brev.fil)
        assertThat(arkivertDokument.filtype).isEqualTo(Filtype.PDFA)
        assertThat(arkivertDokument.dokumenttype).isEqualTo(Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV)
        assertThat(arkivertDokument.tittel).isEqualTo(brev.brevtype.tittel)
        assertThat(arkiverDokumentRequest.fagsakId).isEqualTo(brev.eksternFagsakId.toString())
        assertThat(arkiverDokumentRequest.journalførendeEnhet).isEqualTo(brev.journalførendeEnhet)
    }
}
