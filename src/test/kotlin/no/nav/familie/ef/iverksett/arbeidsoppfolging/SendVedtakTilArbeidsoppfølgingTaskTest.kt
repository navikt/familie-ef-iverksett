package no.nav.familie.ef.iverksett.arbeidsoppfolging

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingsOppgaveForOvergangsstønadTask
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class SendVedtakTilArbeidsoppfølgingTaskTest {

    private val taskService = mockk<TaskService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val arbeidsoppfølgingService = mockk<ArbeidsoppfølgingService>()

    private val task = SendVedtakTilArbeidsoppfølgingTask(taskService, iverksettingRepository, arbeidsoppfølgingService)

    private val taskSlot = CapturingSlot<Task>()

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.findByIdOrThrow(any()) }
            .returns(lagIverksett(opprettIverksettDto(behandlingId = UUID.randomUUID()).toDomain()))
        every { taskService.save(capture(taskSlot)) } answers { firstArg() }
    }

    @Test
    internal fun `doTask - skal publisere vedtak til kafka`() {
        justRun { arbeidsoppfølgingService.sendTilKafka(any()) }

        task.doTask(lagTask())

        verify(exactly = 1) { arbeidsoppfølgingService.sendTilKafka(any()) }
    }

    @Test
    internal fun `onCompletion - skal opprette neste task`() {
        task.onCompletion(lagTask())

        verify(exactly = 1) { taskService.save(any()) }
        Assertions.assertThat(taskSlot.captured.type).isEqualTo(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE)
    }

    @Test
    internal fun `skal mappe stønadstyper`() {
        Assertions.assertThat(StønadType.values().map { it.name }).isEqualTo(StønadType.values().map { it.name })
    }

    private fun lagTask() = Task(SendVedtakTilArbeidsoppfølgingTask.TYPE, UUID.randomUUID().toString())
}
