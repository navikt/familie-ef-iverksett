package no.nav.familie.ef.iverksett.journalføring

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

internal class DistribuerVedtaksbrevTaskTest {

    val journalpostClient = mockk<JournalpostClient>()
    val distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient)

    @Test
    internal fun `skal distribuere brev`() {
        val behandlingId = UUID.randomUUID()
        val journalpostId = "123456789"
        val task = DistribuerVedtaksbrevTask.opprettTask(behandlingId, journalpostId)
        every { journalpostClient.distribuerBrev(journalpostId) } returns "ok"
        distribuerVedtaksbrevTask.doTask(task)
        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId) }
    }
}