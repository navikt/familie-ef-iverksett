package no.nav.familie.ef.iverksett.brukernotifikasjon

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class SendBrukernotifikasjonVedGOmregningTaskTest {
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val brukernotifikasjonKafkaProducer = mockk<BrukernotifikasjonKafkaProducer>()
    private val taskService = mockk<TaskService>()
    private val task = SendBrukernotifikasjonVedGOmregningTask(brukernotifikasjonKafkaProducer, iverksettingRepository, taskService)

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.findByIdOrThrow(any()) }
            .returns(
                lagIverksett(
                    opprettIverksettDto(behandlingId = UUID.randomUUID(), behandlingÅrsak = BehandlingÅrsak.G_OMREGNING).toDomain(),
                ),
            )
    }

    @Test
    internal fun `doTask - skal publisere vedtak til kafka`() {
        every { brukernotifikasjonKafkaProducer.sendBeskjedTilBruker(any(), any()) } just runs
        task.doTask(Task(SendBrukernotifikasjonVedGOmregningTask.TYPE, UUID.randomUUID().toString()))

        verify(exactly = 1) { brukernotifikasjonKafkaProducer.sendBeskjedTilBruker(any(), any()) }
    }
}
