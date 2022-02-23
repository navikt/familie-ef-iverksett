package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class DistribuerVedtaksbrevTaskTest {

    private val journalpostClient = mockk<JournalpostClient>()
    private val tilstandRepository = mockk<TilstandRepository>()
    private val featureToggleService = mockk<FeatureToggleService>()
    private val distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient, tilstandRepository, featureToggleService)

    @BeforeEach
    fun setUp() {
        every { featureToggleService.isEnabled(any()) } returns true
    }

    @Test
    internal fun `skal distribuere brev`() {
        val behandlingId = UUID.randomUUID()
        val journalpostId = "123456789"
        val bestillingId = "111"
        val distribuerVedtaksbrevResultat = slot<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns mapOf("123" to JournalpostResultat(journalpostId))
        every { tilstandRepository.hentTilbakekrevingResultat(behandlingId) } returns null
        every { journalpostClient.distribuerBrev(journalpostId) } returns bestillingId
        every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null andThen mapOf(journalpostId to DistribuerVedtaksbrevResultat(bestillingId))
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                    behandlingId,
                    any(),
                    capture(distribuerVedtaksbrevResultat)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId) }
        verify(exactly = 1) { tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId, any(), any()) }
        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingId)
        assertThat(distribuerVedtaksbrevResultat.captured.dato).isNotNull()
    }

    @Test
    fun `skal distribuere brev med flere mottakere`() {
        val behandlingId = UUID.randomUUID()
        val journalpostResultater = listOf(JournalpostResultat("123456789"), JournalpostResultat("987654321"))
        val bestillingIder = listOf("111", "222")
        val distribuerVedtaksbrevResultatSlots = mutableListOf<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns mapOf("1" to journalpostResultater[0],
                                                                                         "2" to journalpostResultater[1])
        every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
        every { journalpostClient.distribuerBrev(any()) } returns bestillingIder[0] andThen bestillingIder[1]
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                    behandlingId,
                    any(),
                    capture(distribuerVedtaksbrevResultatSlots)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostResultater[0].journalpostId) }
        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostResultater[1].journalpostId) }
        verify(exactly = 2) {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId,
                                                                     any(),
                                                                     any())
        }
        assertThat(distribuerVedtaksbrevResultatSlots.containsAll(bestillingIder.map { DistribuerVedtaksbrevResultat(it) }))
    }

    @Test
    fun `skal kun distribuere brev til mottakere som ikke allerede er distribuert til`(){

        val behandlingId = UUID.randomUUID()
        val identMottakerA = "123"

        val distribuertBestillingId = "abc"
        val distribuertJournalpost = "123456789"
        val ikkeDistribuertJournalpost = "987654321"

        val journalpostResultater = mapOf(identMottakerA to JournalpostResultat(distribuertJournalpost), "456" to JournalpostResultat(
                ikkeDistribuertJournalpost))
        val distribuerteJournalposter = mapOf(journalpostResultater[identMottakerA]!!.journalpostId to DistribuerVedtaksbrevResultat(distribuertBestillingId))
        val ikkeDistrbuertJournalpostBestillingId = "ny bestillingId"

        val journalpostSlot = slot<String>()
        val distribuerVedtaksbrevResultatSlot = slot<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns journalpostResultater
        every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns distribuerteJournalposter
        every { journalpostClient.distribuerBrev(capture(journalpostSlot)) } returns ikkeDistrbuertJournalpostBestillingId
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                    behandlingId,
                    ikkeDistribuertJournalpost,
                    capture(distribuerVedtaksbrevResultatSlot)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 0) { journalpostClient.distribuerBrev(distribuertJournalpost) }
        verify(exactly = 1) { journalpostClient.distribuerBrev(ikkeDistribuertJournalpost) }
        verify(exactly = 0) {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId,
                                                                                    distribuertJournalpost,
                                                                                    any())
        }
        verify(exactly = 1) {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId,
                                                                                    ikkeDistribuertJournalpost,
                                                                                    any())
        }
        assertThat(distribuerVedtaksbrevResultatSlot.captured.bestillingId).isEqualTo(ikkeDistrbuertJournalpostBestillingId)

    }

}
