package no.nav.familie.ef.iverksett.journalføring

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class JournalførVedtaksbrevTaskTest {

    val iverksettingRepository = mockk<IverksettingRepository>()
    val journalpostClient = mockk<JournalpostClient>()
    val taskRepository = mockk<TaskRepository>()
    val tilstandRepository = mockk<TilstandRepository>()
    val journalførVedtaksbrevTask =
            JournalførVedtaksbrevTask(iverksettingRepository, journalpostClient, taskRepository, tilstandRepository)
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
        every { iverksettingRepository.hent(behandlingId) }.returns(opprettIverksettDto(behandlingId = behandlingId).toDomain())
        every { iverksettingRepository.hentBrev(behandlingId) }.returns(Brev(behandlingId, ByteArray(256)))
        every { tilstandRepository.oppdaterJournalpostResultat(behandlingId, capture(journalpostResultatSlot)) } returns Unit

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any()) }
        verify(exactly = 1) { tilstandRepository.oppdaterJournalpostResultat(behandlingId, any()) }
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