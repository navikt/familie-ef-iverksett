package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.util.opprettIverksettBarnetilsyn
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OpprettOppfølgingsOppgaveTaskTest {

    private val oppgaveService = mockk<OppgaveService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskRepository = mockk<TaskRepository>()

    private val taskService = OpprettOppfølgingsOppgaveTask(
            oppgaveService,
            iverksettingRepository,
            taskRepository,
            mockFeatureToggleService(),
    )

    @Test
    internal fun `skal opprette oppfølgningsoppgave for overgangsstønad`() {
        every { iverksettingRepository.hent(any()) } returns opprettIverksett()
        every { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) } returns true
        every { oppgaveService.opprettVurderHenvendelseOppgave(any()) } returns 1

        taskService.doTask(opprettTask())

        verify(exactly = 1) { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) }
        verify(exactly = 1) { oppgaveService.opprettVurderHenvendelseOppgave(any()) }
    }

    @Test
    internal fun `skal ikke opprette oppfølgningsoppgave for barnetilsyn`() {
        every { iverksettingRepository.hent(any()) } returns opprettIverksettBarnetilsyn()

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

    private fun opprettTask() = Task(OpprettOppfølgingsOppgaveTask.TYPE, UUID.randomUUID().toString())
}