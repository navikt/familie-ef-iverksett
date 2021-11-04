package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.tilbakekreving.OpprettTilbakekrevingTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class DistribuerVedtaksbrevTaskTest {

    private val journalpostClient = mockk<JournalpostClient>()
    private val tilstandRepository = mockk<TilstandRepository>()
    private val distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient, tilstandRepository)

    @Test
    internal fun `skal distribuere brev`() {
        val behandlingId = UUID.randomUUID()
        val journalpostId = "123456789"
        val bestillingId = "111"
        val distribuerVedtaksbrevResultat = slot<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns JournalpostResultat(
                journalpostId,
                LocalDateTime.now()
        )
        every { journalpostClient.distribuerBrev(journalpostId) } returns bestillingId
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                    behandlingId,
                    capture(distribuerVedtaksbrevResultat)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId) }
        verify(exactly = 1) { tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId, any()) }
        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingId)
        assertThat(distribuerVedtaksbrevResultat.captured.dato).isNotNull()
    }

}
