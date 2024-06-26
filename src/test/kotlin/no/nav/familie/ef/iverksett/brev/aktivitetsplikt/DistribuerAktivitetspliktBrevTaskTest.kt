package no.nav.familie.ef.iverksett.brev.aktivitetsplikt

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.DistribuerJournalpostResponseTo
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.AktivitetspliktInnhentingBrevUtil.opprettBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.vedtakstatistikk.toJson
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.RekjørSenereException
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.util.UUID

internal class DistribuerAktivitetspliktBrevTaskTest {
    private val journalpostClient = mockk<JournalpostClient>()
    private val taskService = mockk<TaskService>()
    private val aktivitetspliktBrevRepository = mockk<AktivitetspliktBrevRepository>()

    private val distribuerTask =
        DistribuerAktivitetspliktBrevTask(aktivitetspliktBrevRepository, journalpostClient, taskService)

    private val brevId = UUID.randomUUID().toString()

    private val journalpostIdSlot = slot<String>()

    @BeforeEach
    internal fun setUp() {
        every { taskService.findTaskLoggByTaskId(any()) } returns emptyList()
    }

    @Test
    internal fun `skal kaste feil dersom brevet ikke er journalført`() {
        every { aktivitetspliktBrevRepository.findByIdOrThrow(any()) } returns opprettBrev()
        val feil =
            assertThrows<IllegalStateException> { distribuerTask.doTask(Task(DistribuerAktivitetspliktBrevTask.TYPE, brevId)) }

        assertThat(feil.message).contains(
            "Distribuering av brev for innhenting av aktivitetsplikt " +
                "med id=$brevId feilet. Fant ingen journalpostId på brevet.",
        )
        verify(exactly = 0) { journalpostClient.distribuerBrev(any(), any()) }
    }

    @Test
    internal fun `skal ikke distribuere brev ved avdød person men feile tasken`() {
        every { aktivitetspliktBrevRepository.findByIdOrThrow(any()) } returns opprettBrev("journalpostId")
        every { journalpostClient.distribuerBrev("journalpostId", any()) } throws ressursExceptionGone()

        val throwable =
            Assertions.catchThrowable {
                distribuerTask.doTask(Task(DistribuerAktivitetspliktBrevTask.TYPE, brevId))
            }
        assertThat(throwable).isInstanceOf(RekjørSenereException::class.java)

        val rekjørSenereException = throwable as RekjørSenereException
        assertThat(rekjørSenereException.triggerTid)
            .isBetween(LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8))
        assertThat(rekjørSenereException.årsak).startsWith("Dødsbo")
        verify(exactly = 1) { journalpostClient.distribuerBrev(any(), Distribusjonstype.VIKTIG) }
    }

    @Test
    internal fun `skal ferdigstille task ved Conflict exception`() {
        every { aktivitetspliktBrevRepository.findByIdOrThrow(any()) } returns opprettBrev("journalpostId")
        every { journalpostClient.distribuerBrev(capture(journalpostIdSlot), any()) } throws ressursExceptionConflict("bestillingId")

        distribuerTask.doTask(Task(DistribuerAktivitetspliktBrevTask.TYPE, brevId))

        verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
        verify(exactly = 0) { taskService.findTaskLoggByTaskId(any()) }
        assertThat(journalpostIdSlot.captured).isEqualTo("journalpostId")
    }

    @Test
    internal fun `skal distribuere brev for innhenting av aktivitetsplikt`() {
        every { aktivitetspliktBrevRepository.findByIdOrThrow(any()) } returns opprettBrev("journalpostId")
        every { journalpostClient.distribuerBrev(capture(journalpostIdSlot), any()) } returns "bestillingId"

        distribuerTask.doTask(Task(DistribuerAktivitetspliktBrevTask.TYPE, brevId))

        verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
        verify(exactly = 0) { taskService.findTaskLoggByTaskId(any()) }
        assertThat(journalpostIdSlot.captured).isEqualTo("journalpostId")
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
