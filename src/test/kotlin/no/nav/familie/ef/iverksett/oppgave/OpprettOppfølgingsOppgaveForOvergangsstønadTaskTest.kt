package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.opprettIverksettBarnetilsyn
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OpprettOppfølgingsOppgaveForOvergangsstønadTaskTest {

    private val oppgaveService = mockk<OppgaveService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskRepository = mockk<TaskRepository>()

    private val taskService = OpprettOppfølgingsOppgaveForOvergangsstønadTask(
        oppgaveService,
        iverksettingRepository,
        taskRepository
    )

    @Test
    internal fun `skal opprette oppfølgningsoppgave for overgangsstønad`() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettOvergangsstønad())
        every { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) } returns true
        every { oppgaveService.opprettVurderHenvendelseOppgave(any()) } returns 1

        taskService.doTask(opprettTask())

        verify(exactly = 1) { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) }
        verify(exactly = 1) { oppgaveService.opprettVurderHenvendelseOppgave(any()) }
    }

    @Test
    internal fun `skal ikke opprette oppfølgningsoppgave for barnetilsyn`() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettBarnetilsyn())

        taskService.doTask(opprettTask())

        verify(exactly = 0) { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) }
    }

    @Test
    internal fun `onCompletion oppretter neste task i flyten`() {
        every { taskRepository.save(any()) } answers { firstArg() }
        val task = opprettTask()
        taskService.onCompletion(task)
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    private fun opprettTask() = Task(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE, UUID.randomUUID().toString())
}
