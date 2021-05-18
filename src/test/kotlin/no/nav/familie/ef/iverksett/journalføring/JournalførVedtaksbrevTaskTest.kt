package no.nav.familie.ef.iverksett.journalføring

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
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
    val journalførVedtaksbrevTask = JournalførVedtaksbrevTask(hentIverksettService, journalpostClient, taskRepository)

    @Test
    internal fun `skal journalføre brev og opprette ny task`() {
        val behandlingId = UUID.randomUUID()
        val behandlingIdString = behandlingId.toString()
        val journalpostId = "123456789"
        val arkiverDokumentRequestSlot = slot<ArkiverDokumentRequest>()
        val distribuerVedtaksbrevTask = slot<Task>()

        every { journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot)) } returns ArkiverDokumentResponse(
                journalpostId,
                true)
        every { hentIverksettService.hentIverksett(behandlingId) }.returns(opprettIverksettDto(behandlingId = behandlingId).toDomain())
        every { hentIverksettService.hentBrev(behandlingId) }.returns(Brev(behandlingId, ByteArray(256)))
        every { taskRepository.save(capture(distribuerVedtaksbrevTask)) } returns Task(DistribuerVedtaksbrevTask.TYPE,
                                                                                       behandlingIdString,
                                                                                       Properties())

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any()) }
        assertThat(arkiverDokumentRequestSlot.captured.hoveddokumentvarianter.size).isEqualTo(1)
        assertThat(distribuerVedtaksbrevTask.captured.payload).contains(behandlingIdString)
        assertThat(distribuerVedtaksbrevTask.captured.payload).contains(journalpostId)
        assertThat(distribuerVedtaksbrevTask.captured.type).isEqualTo(DistribuerVedtaksbrevTask.TYPE)
    }
}