package no.nav.familie.ef.iverksett.journalføring

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class JournalførVedtaksbrevTaskTest {

    val hentIverksettService = mockk<HentIverksettService>()
    val journalpostClient = mockk<JournalpostClient>()
    val taskRepository = mockk<TaskRepository>()
    val lagreTilstandService = mockk<LagreTilstandService>()
    val journalførVedtaksbrevTask =
            JournalførVedtaksbrevTask(hentIverksettService, journalpostClient, taskRepository, lagreTilstandService)
    val behandlingId = UUID.randomUUID()

    @Test
    internal fun `skal journalføre brev og opprette ny task`() {
        val behandlingIdString = behandlingId.toString()
        val journalpostId = "123456789"
        val arkiverDokumentRequestSlot = slot<ArkiverDokumentRequest>()
        val journalpostResultatSlot = slot<JournalpostResultat>()


        every { journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot)) } returns ArkiverDokumentResponse(
                journalpostId,
                true)
        every { hentIverksettService.hentIverksett(behandlingId) }.returns(opprettIverksettDto(behandlingId = behandlingId).toDomain())
        every { hentIverksettService.hentBrev(behandlingId) }.returns(Brev(behandlingId, ByteArray(256)))
        every { lagreTilstandService.lagreJournalPostResultat(behandlingId, capture(journalpostResultatSlot)) } returns Unit

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any()) }
        verify(exactly = 1) { lagreTilstandService.lagreJournalPostResultat(behandlingId, any()) }
        assertThat(arkiverDokumentRequestSlot.captured.hoveddokumentvarianter.size).isEqualTo(1)
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