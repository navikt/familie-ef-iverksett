package no.nav.familie.ef.iverksett.brev

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class JournalførVedtaksbrevTaskTest {

    val iverksettingRepository = mockk<IverksettingRepository>()
    private val journalpostClient = mockk<JournalpostClient>()
    val taskRepository = mockk<TaskRepository>()
    val tilstandRepository = mockk<TilstandRepository>()
    val featureToggleService = mockk<FeatureToggleService>()
    private val journalførVedtaksbrevTask =
            JournalførVedtaksbrevTask(iverksettingRepository, journalpostClient, taskRepository, tilstandRepository, featureToggleService)
    val behandlingId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp(){
        every { featureToggleService.isEnabled(any()) } returns true
    }

    @Test
    internal fun `skal journalføre brev og opprette ny task`() {
        val behandlingIdString = behandlingId.toString()
        val journalpostId = "123456789"
        val arkiverDokumentRequestSlot = slot<ArkiverDokumentRequest>()
        val journalpostResultatSlot = slot<JournalpostResultat>()


        every { journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot), any()) } returns ArkiverDokumentResponse(
                journalpostId,
                true)
        every { iverksettingRepository.hent(behandlingId) }.returns(opprettIverksettDto(behandlingId = behandlingId).toDomain())
        every { iverksettingRepository.hentBrev(behandlingId) }.returns(Brev(behandlingId, ByteArray(256)))
        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns null andThen mapOf("123" to JournalpostResultat(journalpostId))
        every { tilstandRepository.oppdaterJournalpostResultat(behandlingId, any(), capture(journalpostResultatSlot)) } returns Unit

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { tilstandRepository.oppdaterJournalpostResultat(behandlingId, any(), any()) }
        assertThat(arkiverDokumentRequestSlot.captured.hoveddokumentvarianter.size).isEqualTo(1)
        assertThat(journalpostResultatSlot.captured.journalpostId).isEqualTo(journalpostId)
    }

    @Test
    internal fun `skal journalføre brev til alle brevmottakere`() {

        val verge = Brevmottaker("22222222222",
                                 "Mottaker Navn",
                                 Brevmottaker.MottakerRolle.VERGE,
                                 Brevmottaker.IdentType.PERSONIDENT)
        val fullmektig = Brevmottaker("333333333",
                                      "Mottaker B Navn",
                                      Brevmottaker.MottakerRolle.FULLMEKTIG,
                                      Brevmottaker.IdentType.ORGANISASJONSNUMMER)

        val brevmottakere = listOf(
                verge,
                fullmektig)
        val iverksettMedBrevmottakere = opprettIverksettDto(behandlingId).let {
            it.copy(vedtak = it.vedtak.copy(brevmottakere = brevmottakere)).toDomain()
        }

        val capturedArkiverdokumentRequester = mutableListOf<ArkiverDokumentRequest>()

        every { iverksettingRepository.hent(behandlingId) } returns iverksettMedBrevmottakere
        every { iverksettingRepository.hentBrev(behandlingId) }.returns(Brev(behandlingId, ByteArray(256)))
        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns null andThen mapOf("123" to JournalpostResultat(
                "journalpostId"), "444" to JournalpostResultat("journalpostId"))
        every { tilstandRepository.oppdaterJournalpostResultat(behandlingId, any(), any()) } just Runs

        every {
            journalpostClient.arkiverDokument(capture(capturedArkiverdokumentRequester),
                                              any())
        } returns ArkiverDokumentResponse(journalpostId = UUID.randomUUID().toString(), ferdigstilt = true)

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))



        verify(exactly = 2) { journalpostClient.arkiverDokument(any(), any()) }
        assertThat(capturedArkiverdokumentRequester.size).isEqualTo(2)
        assertThat(capturedArkiverdokumentRequester.map { it.avsenderMottaker!!.id }).containsAll(brevmottakere.map { it.ident })

    }

    @Test
    internal fun `Journalføring av barnetilsynbrev og opprette ny task`() {
        val behandlingIdString = behandlingId.toString()
        val journalpostId = "123456789"
        val arkiverDokumentRequestSlot = slot<ArkiverDokumentRequest>()
        val journalpostResultatSlot = slot<JournalpostResultat>()

        every { journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot), any()) } returns ArkiverDokumentResponse(
                journalpostId,
                true)
        every { iverksettingRepository.hent(behandlingId) }.returns(opprettIverksettDto(behandlingId = behandlingId,
                                                                                        stønadType = StønadType.BARNETILSYN).toDomain())
        every { iverksettingRepository.hentBrev(behandlingId) }.returns(Brev(behandlingId, ByteArray(256)))
        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns null andThen mapOf("123" to JournalpostResultat(
                "journalpostId"))
        every {
            tilstandRepository.oppdaterJournalpostResultat(behandlingId,
                                                           any(),
                                                           capture(journalpostResultatSlot))
        } returns Unit

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { tilstandRepository.oppdaterJournalpostResultat(behandlingId, any(), any()) }
        assertThat(arkiverDokumentRequestSlot.captured.hoveddokumentvarianter.size).isEqualTo(1)
        assertThat(arkiverDokumentRequestSlot.captured.hoveddokumentvarianter.first().dokumenttype).isEqualTo(Dokumenttype.VEDTAKSBREV_BARNETILSYN)
        assertThat(journalpostResultatSlot.captured.journalpostId).isEqualTo(journalpostId)
    }


    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val taskSlot = slot<Task>()
        val task = Task(JournalførVedtaksbrevTask.TYPE, behandlingId.toString(), Properties())
        every { taskRepository.save(capture(taskSlot)) } returns task

        journalførVedtaksbrevTask.onCompletion(task)

        assertThat(taskSlot.captured.payload).isEqualTo(behandlingId.toString())
        assertThat(taskSlot.captured.type).isEqualTo(DistribuerVedtaksbrevTask.TYPE)
    }
}