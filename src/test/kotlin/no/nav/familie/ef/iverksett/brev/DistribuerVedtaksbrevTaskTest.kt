package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.prosessering.domene.Loggtype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskLogg
import no.nav.familie.prosessering.error.RekjørSenereException
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class DistribuerVedtaksbrevTaskTest {

    private val journalpostClient = mockk<JournalpostClient>()
    private val tilstandRepository = mockk<TilstandRepository>()
    private val distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient, tilstandRepository)

    private val behandlingId = UUID.randomUUID()
    private val identMottakerA = "123"

    @Test
    internal fun `skal distribuere brev`() {
        val journalpostId = "123456789"
        val bestillingId = "111"
        val distribuerVedtaksbrevResultat = slot<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns mapOf(
            "123" to JournalpostResultat(
                journalpostId
            )
        )
        every { tilstandRepository.hentTilbakekrevingResultat(behandlingId) } returns null
        every { journalpostClient.distribuerBrev(journalpostId, any()) } returns bestillingId
        every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null andThen mapOf(
            journalpostId to DistribuerVedtaksbrevResultat(
                bestillingId
            )
        )
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                capture(distribuerVedtaksbrevResultat)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId, any()) }
        verify(exactly = 1) { tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId, any(), any()) }
        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingId)
        assertThat(distribuerVedtaksbrevResultat.captured.dato).isNotNull()
    }

    @Test
    fun `skal distribuere brev med flere mottakere`() {
        val journalpostResultater = listOf(JournalpostResultat("123456789"), JournalpostResultat("987654321"))
        val bestillingIder = listOf("111", "222")
        val distribuerVedtaksbrevResultatSlots = mutableListOf<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns mapOf(
            "1" to journalpostResultater[0],
            "2" to journalpostResultater[1]
        )
        every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
        every { journalpostClient.distribuerBrev(any(), any()) } returns bestillingIder[0] andThen bestillingIder[1]
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                capture(distribuerVedtaksbrevResultatSlots)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostResultater[0].journalpostId, any()) }
        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostResultater[1].journalpostId, any()) }
        verify(exactly = 2) {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                any()
            )
        }
        assertThat(distribuerVedtaksbrevResultatSlots.containsAll(bestillingIder.map { DistribuerVedtaksbrevResultat(it) }))
    }

    @Test
    fun `skal kun distribuere brev til mottakere som ikke allerede er distribuert til`() {
        val identMottakerA = "123"

        val distribuertBestillingId = "abc"
        val distribuertJournalpost = "123456789"
        val ikkeDistribuertJournalpost = "987654321"

        val journalpostResultater =
            mapOf(
                identMottakerA to JournalpostResultat(distribuertJournalpost),
                "456" to JournalpostResultat(
                    ikkeDistribuertJournalpost
                )
            )
        val distribuerteJournalposter =
            mapOf(
                journalpostResultater[identMottakerA]!!.journalpostId to DistribuerVedtaksbrevResultat(
                    distribuertBestillingId
                )
            )
        val ikkeDistrbuertJournalpostBestillingId = "ny bestillingId"

        val journalpostSlot = slot<String>()
        val distribuerVedtaksbrevResultatSlot = slot<DistribuerVedtaksbrevResultat>()

        every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns journalpostResultater
        every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns distribuerteJournalposter
        every { journalpostClient.distribuerBrev(capture(journalpostSlot), any()) } returns ikkeDistrbuertJournalpostBestillingId
        every {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                ikkeDistribuertJournalpost,
                capture(distribuerVedtaksbrevResultatSlot)
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 0) { journalpostClient.distribuerBrev(distribuertJournalpost, any()) }
        verify(exactly = 1) { journalpostClient.distribuerBrev(ikkeDistribuertJournalpost, any()) }
        verify(exactly = 0) {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                distribuertJournalpost,
                any()
            )
        }
        verify(exactly = 1) {
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                ikkeDistribuertJournalpost,
                any()
            )
        }
        assertThat(distribuerVedtaksbrevResultatSlot.captured.bestillingId).isEqualTo(ikkeDistrbuertJournalpostBestillingId)
    }

    @Nested
    inner class `Død person` {

        @Test
        internal fun `skal rekjøre senere hvis man får GONE fra dokdist`() {
            val journalpostResultater = listOf(JournalpostResultat("123456789"))

            every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns
                mapOf("1" to journalpostResultater[0])
            every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
            every { journalpostClient.distribuerBrev(any(), any()) } throws ressursExceptionGone()

            val throwable = catchThrowable {
                distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString()))
            }
            assertThat(throwable).isInstanceOf(RekjørSenereException::class.java)
            val rekjørSenereException = throwable as RekjørSenereException
            assertThat(rekjørSenereException.triggerTid)
                .isBetween(LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8))
            assertThat(rekjørSenereException.årsak).startsWith("Dødsbo")

            verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
            verify(exactly = 0) { tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(any(), any(), any()) }
        }

        @Test
        internal fun `skal feile hvis man har blitt kjørt fler enn 7 ganger`() {
            val journalpostResultater = listOf(JournalpostResultat("123456789"))

            every { tilstandRepository.hentJournalpostResultat(behandlingId) } returns mapOf("1" to journalpostResultater[0])
            every { tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
            every { journalpostClient.distribuerBrev(any(), any()) } throws ressursExceptionGone()

            val throwable = catchThrowable {
                val task = Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString())
                val taskLogg = IntRange(1, 8).map { TaskLogg(type = Loggtype.KLAR_TIL_PLUKK, melding = "Dødsbo") }
                distribuerVedtaksbrevTask.doTask(task.copy(logg = task.logg + taskLogg))
            }
            assertThat(throwable).isInstanceOf(TaskExceptionUtenStackTrace::class.java)
            assertThat(throwable).hasMessageStartingWith("Er dødsbo og har feilet flere ganger")

            verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
            verify(exactly = 0) { tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(any(), any(), any()) }
        }
    }

    private fun ressursExceptionGone() =
        RessursException(
            Ressurs.failure(""),
            HttpClientErrorException.create(HttpStatus.GONE, "", HttpHeaders(), byteArrayOf(), null)
        )
}
