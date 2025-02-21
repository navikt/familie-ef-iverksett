package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.OppgaverForOpprettelse
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.opprettIverksettBarnetilsyn
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.util.vedtaksdetaljerOvergangsstønad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Test
import java.util.UUID

class OppgaverForOpprettelseForOvergangsstønadTaskTest {
    private val oppgaveService = mockk<OppgaveService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskService = mockk<TaskService>()

    private val taskStegService =
        OpprettFremleggsoppgaveForOvergangsstønadTask(
            oppgaveService,
            iverksettingRepository,
            taskService,
        )

    @Test
    internal fun `skal opprette fremleggsoppgave for overgangsstønad`() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettOvergangsstønad())
        every { oppgaveService.opprettFremleggsoppgave(any(), any(), any()) } returns 1

        taskStegService.doTask(opprettTask())

        verify(exactly = 1) { oppgaveService.opprettFremleggsoppgave(any(), any(), any()) }
    }

    @Test
    internal fun `skal ikke opprette fremleggsoppgave for overgangsstønad`() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns
            lagIverksett(
                opprettIverksettOvergangsstønad(
                    vedtaksdetaljer =
                        vedtaksdetaljerOvergangsstønad(
                            oppgaverForOpprettelse =
                                OppgaverForOpprettelse(
                                    emptyList(),
                                ),
                        ),
                ),
            )
        every { oppgaveService.opprettFremleggsoppgave(any(), any(), any()) } returns 1

        taskStegService.doTask(opprettTask())

        verify(exactly = 0) { oppgaveService.opprettFremleggsoppgave(any(), any(), any()) }
    }

    @Test
    internal fun `skal ikke opprette fremleggsoppgave for stønad som ikke er overgangsstønad`() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettBarnetilsyn())

        taskStegService.doTask(opprettTask())

        verify(exactly = 0) { oppgaveService.opprettFremleggsoppgave(any(), any(), any()) }
    }

    @Test
    internal fun `onCompletion oppretter neste task i flyten`() {
        every { taskService.save(any()) } answers { firstArg() }
        val task = opprettTask()
        taskStegService.onCompletion(task)
        verify(exactly = 1) { taskService.save(any()) }
    }

    private fun opprettTask() = Task(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE, UUID.randomUUID().toString())
}
