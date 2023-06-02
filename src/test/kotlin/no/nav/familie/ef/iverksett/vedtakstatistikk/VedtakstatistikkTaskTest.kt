package no.nav.familie.ef.iverksett.vedtakstatistikk

import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.brukernotifikasjon.SendBrukernotifikasjonVedGOmregningTask
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class VedtakstatistikkTaskTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val vedtakstatistikkService = mockk<VedtakstatistikkService>()
    private val taskService = mockk<TaskService>()
    private val vedtakstatistikkTask =
        VedtakstatistikkTask(iverksettingRepository, vedtakstatistikkService, taskService, mockFeatureToggleService())
    private val taskSlot = CapturingSlot<Task>()
    private val behandlingId: UUID = UUID.randomUUID()

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.findByIdOrThrow(any()) }
            .returns(lagIverksett(opprettIverksettDto(behandlingId = UUID.randomUUID()).toDomain()))
        every { taskService.save(capture(taskSlot)) } answers { firstArg() }
    }

    @Test
    fun `skal sende vedtaksstatistikk til DVH`() {
        val behandlingIdString = behandlingId.toString()
        val returnIverksetting = lagIverksett(opprettIverksettDto(behandlingId = behandlingId).toDomain())

        every { vedtakstatistikkService.sendTilKafka(returnIverksetting.data, null) } just Runs
        every { iverksettingRepository.findByIdOrThrow(behandlingId) }
            .returns(returnIverksetting)

        vedtakstatistikkTask.doTask(Task(VedtakstatistikkTask.TYPE, behandlingIdString, Properties()))
        verify(exactly = 1) { vedtakstatistikkService.sendTilKafka(returnIverksetting.data, null) }
    }

    @Test
    internal fun `onCompletion - skal opprette neste task hvis G-omregning`() {
        every { iverksettingRepository.findByIdOrThrow(any()) }
            .returns(lagIverksett(opprettIverksettDto(behandlingId = UUID.randomUUID(), behandlingÅrsak = BehandlingÅrsak.G_OMREGNING).toDomain()))
        vedtakstatistikkTask.onCompletion(Task(VedtakstatistikkTask.TYPE, UUID.randomUUID().toString()))

        verify(exactly = 1) { taskService.save(any()) }
        Assertions.assertThat(taskSlot.captured.type).isEqualTo(SendBrukernotifikasjonVedGOmregningTask.TYPE)
    }

    @Test
    internal fun `onCompletion - skal ikke opprette neste task hvis ikke G-omregning`() {
        vedtakstatistikkTask.onCompletion(Task(VedtakstatistikkTask.TYPE, UUID.randomUUID().toString()))
        verify(exactly = 0) { taskService.save(any()) }
    }
}
