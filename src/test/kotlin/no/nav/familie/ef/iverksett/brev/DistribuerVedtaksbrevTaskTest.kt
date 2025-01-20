package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.vedtakstatistikk.toJson
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.prosessering.domene.Loggtype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskLogg
import no.nav.familie.prosessering.error.RekjørSenereException
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
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
    private val iverksettResultatService = mockk<IverksettResultatService>()
    private val taskService = mockk<TaskService>()
    private val distribuerVedtaksbrevTask =
        DistribuerVedtaksbrevTask(journalpostClient, iverksettResultatService, taskService)

    private val behandlingId = UUID.randomUUID()
    private val identMottakerA = "123"

    @BeforeEach
    internal fun setUp() {
        every { taskService.findTaskLoggByTaskId(any()) } returns emptyList()
    }

    @Test
    internal fun `skal distribuere brev`() {
        val journalpostId = "123456789"
        val bestillingId = "111"
        val distribuerVedtaksbrevResultat = slot<DistribuerBrevResultat>()

        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns
            mapOf(
                "123" to
                    JournalpostResultat(
                        journalpostId,
                    ),
            )
        every { iverksettResultatService.hentTilbakekrevingResultat(behandlingId) } returns null
        every { journalpostClient.distribuerBrev(journalpostId, any()) } returns bestillingId
        every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null andThen
            mapOf(
                journalpostId to
                    DistribuerBrevResultat(
                        bestillingId,
                    ),
            )
        every {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                capture(distribuerVedtaksbrevResultat),
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostId, any()) }
        verify(exactly = 1) { iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(behandlingId, any(), any()) }
        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingId)
        assertThat(distribuerVedtaksbrevResultat.captured.dato).isNotNull()
    }

    @Test
    internal fun `skal oppdatere resultat med bestillingId når vi får exeption av type conflict - brev distribuert tidligere `() {
        val journalpostId = "123456789"
        val bestillingId = "111"
        val distribuerVedtaksbrevResultat = slot<DistribuerBrevResultat>()

        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns
            mapOf(
                "123" to
                    JournalpostResultat(
                        journalpostId,
                    ),
            )
        every { iverksettResultatService.hentTilbakekrevingResultat(behandlingId) } returns null
        val bestillingsIdFraConflictException = "BestillingsId"
        every { journalpostClient.distribuerBrev(any(), any()) } throws ressursExceptionConflict(bestillingsIdFraConflictException)
        every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null andThen
            mapOf(
                journalpostId to
                    DistribuerBrevResultat(
                        bestillingId,
                    ),
            )
        justRun {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                capture(distribuerVedtaksbrevResultat),
            )
        }
        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        assertThat(distribuerVedtaksbrevResultat.captured.bestillingId).isEqualTo(bestillingsIdFraConflictException)
    }

    @Test
    fun `skal distribuere brev med flere mottakere`() {
        val journalpostResultater = listOf(JournalpostResultat("123456789"), JournalpostResultat("987654321"))
        val bestillingIder = listOf("111", "222")
        val distribuerVedtaksbrevResultatSlots = mutableListOf<DistribuerBrevResultat>()

        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns
            mapOf(
                "1" to journalpostResultater[0],
                "2" to journalpostResultater[1],
            )
        every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
        every { journalpostClient.distribuerBrev(any(), any()) } returns bestillingIder[0] andThen bestillingIder[1]
        every {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                capture(distribuerVedtaksbrevResultatSlots),
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostResultater[0].journalpostId, any()) }
        verify(exactly = 1) { journalpostClient.distribuerBrev(journalpostResultater[1].journalpostId, any()) }
        verify(exactly = 2) {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                any(),
            )
        }
        assertThat(distribuerVedtaksbrevResultatSlots.containsAll(bestillingIder.map { DistribuerBrevResultat(it) }))
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
                "456" to
                    JournalpostResultat(
                        ikkeDistribuertJournalpost,
                    ),
            )
        val distribuerteJournalposter =
            mapOf(
                journalpostResultater[identMottakerA]!!.journalpostId to
                    DistribuerBrevResultat(
                        distribuertBestillingId,
                    ),
            )
        val ikkeDistrbuertJournalpostBestillingId = "ny bestillingId"

        val journalpostSlot = slot<String>()
        val distribuerVedtaksbrevResultatSlot = slot<DistribuerBrevResultat>()

        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns journalpostResultater
        every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns distribuerteJournalposter
        every { journalpostClient.distribuerBrev(capture(journalpostSlot), any()) } returns ikkeDistrbuertJournalpostBestillingId
        every {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                ikkeDistribuertJournalpost,
                capture(distribuerVedtaksbrevResultatSlot),
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 0) { journalpostClient.distribuerBrev(distribuertJournalpost, any()) }
        verify(exactly = 1) { journalpostClient.distribuerBrev(ikkeDistribuertJournalpost, any()) }
        verify(exactly = 0) {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                distribuertJournalpost,
                any(),
            )
        }
        verify(exactly = 1) {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                ikkeDistribuertJournalpost,
                any(),
            )
        }
        assertThat(distribuerVedtaksbrevResultatSlot.captured.bestillingId).isEqualTo(ikkeDistrbuertJournalpostBestillingId)
    }


    @Test
    fun `skal distribuere brev til mottakere som allerede er distribuert til dersom de ligger i spesiallista`() {
        val identMottakerA = "123"

        val distribuertBestillingId = "abc"
        val distribuertJournalpost = "692172785"

        val journalpostResultater =
            mapOf(
                identMottakerA to JournalpostResultat(distribuertJournalpost),
            )
        val distribuerteJournalposter =
            mapOf(
                journalpostResultater[identMottakerA]!!.journalpostId to
                    DistribuerBrevResultat(
                        distribuertBestillingId,
                    ),
            )
        val nyBestillingId = "ny bestillingId"

        val journalpostSlot = slot<String>()
        val distribuerVedtaksbrevResultatSlot = slot<DistribuerBrevResultat>()

        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns journalpostResultater
        every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns distribuerteJournalposter
        every { journalpostClient.distribuerBrev(capture(journalpostSlot), any()) } returns nyBestillingId
        every {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                any(),
                capture(distribuerVedtaksbrevResultatSlot),
            )
        } returns Unit

        distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(distribuertJournalpost, any()) }
        verify(exactly = 1) {
            iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
                behandlingId,
                distribuertJournalpost,
                any(),
            )
        }
        assertThat(distribuerVedtaksbrevResultatSlot.captured.bestillingId).isEqualTo(nyBestillingId)
    }

    @Nested
    inner class `Død person` {
        @Test
        internal fun `skal rekjøre senere hvis man får GONE fra dokdist`() {
            val journalpostResultater = listOf(JournalpostResultat("123456789"))

            every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns
                mapOf("1" to journalpostResultater[0])
            every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
            every { journalpostClient.distribuerBrev(any(), any()) } throws ressursExceptionGone()

            val throwable =
                catchThrowable {
                    distribuerVedtaksbrevTask.doTask(Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString()))
                }
            assertThat(throwable).isInstanceOf(RekjørSenereException::class.java)
            val rekjørSenereException = throwable as RekjørSenereException
            assertThat(rekjørSenereException.triggerTid)
                .isBetween(LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8))
            assertThat(rekjørSenereException.årsak).startsWith("Dødsbo")

            verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
            verify(exactly = 0) { iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(any(), any(), any()) }
        }

        @Test
        internal fun `skal feile hvis man har blitt kjørt fler enn 26 ganger`() {
            val journalpostResultater = listOf(JournalpostResultat("123456789"))

            val task = Task(DistribuerVedtaksbrevTask.TYPE, behandlingId.toString())
            val taskLogg =
                IntRange(1, 27).map { TaskLogg(taskId = task.id, type = Loggtype.KLAR_TIL_PLUKK, melding = "Dødsbo") }

            every { iverksettResultatService.hentJournalpostResultat(behandlingId) }
                .returns(mapOf("1" to journalpostResultater[0]))
            every { iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId) } returns null
            every { journalpostClient.distribuerBrev(any(), any()) } throws ressursExceptionGone()
            every { taskService.findTaskLoggByTaskId(any()) } returns taskLogg

            val throwable = catchThrowable { distribuerVedtaksbrevTask.doTask(task) }
            assertThat(throwable).isInstanceOf(TaskExceptionUtenStackTrace::class.java)
            assertThat(throwable).hasMessageStartingWith("Er dødsbo og har feilet flere ganger")

            verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
            verify(exactly = 0) { iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(any(), any(), any()) }
        }
    }

    private fun ressursExceptionGone() =
        RessursException(
            Ressurs.failure(""),
            HttpClientErrorException.create(HttpStatus.GONE, "", HttpHeaders(), byteArrayOf(), null),
        )

    private fun ressursExceptionConflict(bestillingsId: String): RessursException {
        val e =
            HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "",
                HttpHeaders(),
                DistribuerJournalpostResponseTo(bestillingsId).toJson().toByteArray(),
                null,
            )

        val ressurs: Ressurs<Any> =
            Ressurs(
                data = e.responseBodyAsString,
                status = Ressurs.Status.FEILET,
                melding = e.message.toString(),
                stacktrace = e.stackTraceToString(),
            )

        return RessursException(
            ressurs,
            e,
        )
    }
}
