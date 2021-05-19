package no.nav.familie.ef.iverksett.journalføring

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class DistribuerVedtaksbrevTaskTest {

    val journalpostClient = mockk<JournalpostClient>()
    val lagreTilstandService = mockk<LagreTilstandService>()
    val distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient, lagreTilstandService)

    @Test
    internal fun `skal distribuere brev`() {
        val behandlingId = UUID.randomUUID()
        val journalpostId = "123456789"
        val bestillingId = "111"
        val task = DistribuerVedtaksbrevTask.opprettTask(behandlingId, journalpostId)
        val distribuerVedtaksbrevResultat = slot<DistribuerVedtaksbrevResultat>()

        every { journalpostClient.distribuerBrev(journalpostId) } returns bestillingId
        every { lagreTilstandService.lagreDistribuerVedtaksbrevResultat(behandlingId, capture(distribuerVedtaksbrevResultat)) } returns Unit
        
        distribuerVedtaksbrevTask.doTask(task)

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId) }
        verify(exactly = 1) { lagreTilstandService.lagreDistribuerVedtaksbrevResultat(behandlingId, any())}
        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingId)
        assertThat(distribuerVedtaksbrevResultat.captured.dato).isNotNull()
    }
}