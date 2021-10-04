package no.nav.familie.ef.iverksett.vedtak

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class PubliserVedtakTilKafkaTaskTest {

    private val taskRepository = mockk<TaskRepository>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val vedtakKafkaProducer = mockk<VedtakKafkaProducer>()

    private val task = PubliserVedtakTilKafkaTask(taskRepository, iverksettingRepository, vedtakKafkaProducer)

    private val taskSlot = CapturingSlot<Task>()

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.hent(any()) } returns opprettIverksettDto(behandlingId = UUID.randomUUID()).toDomain()
        every { taskRepository.save(capture(taskSlot)) } answers { firstArg() }
    }

    @Test
    internal fun `doTask - ska publisere vedtak til kafka`() {
        task.doTask(lagTask())

        verify(exactly = 1) { vedtakKafkaProducer.sendVedtak(any()) }
    }

    @Test
    internal fun `onCompletion - skal opprette neste task`() {
        task.onCompletion(lagTask())

        verify(exactly = 1) { taskRepository.save(any()) }
        assertThat(taskSlot.captured.type).isEqualTo(OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad.TYPE)
    }

    private fun lagTask() = Task(PubliserVedtakTilKafkaTask.TYPE, UUID.randomUUID().toString())
}