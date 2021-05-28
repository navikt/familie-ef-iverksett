package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandDbUtil
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class DistribuerVedtaksbrevTaskTest {

    val journalpostClient = mockk<JournalpostClient>()
    val lagreTilstandService = mockk<TilstandDbUtil>()
    val tilstandDbUtil = mockk<TilstandDbUtil>()
    val distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient, lagreTilstandService, tilstandDbUtil)

    @Test
    internal fun `skal distribuere brev`() {
        val behandlingId = UUID.randomUUID()
        val journalpostId = "123456789"
        val bestillingId = "111"
        val distribuerVedtaksbrevResultat = slot<DistribuerVedtaksbrevResultat>()

        every { tilstandDbUtil.hentJournalpostResultat(behandlingId) } returns JournalpostResultat(journalpostId,
                                                                                                        LocalDateTime.now())
        every { journalpostClient.distribuerBrev(journalpostId) } returns bestillingId
        every { lagreTilstandService.lagreDistribuerVedtaksbrevResultat(behandlingId, capture(distribuerVedtaksbrevResultat)) } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId) }
        verify(exactly = 1) { lagreTilstandService.lagreDistribuerVedtaksbrevResultat(behandlingId, any()) }
        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingId)
        assertThat(distribuerVedtaksbrevResultat.captured.dato).isNotNull()
    }
}