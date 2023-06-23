package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class IverksettMotOppdragTaskTest {

    private val oppdragClient = mockk<OppdragClient>()
    val taskService = mockk<TaskService>()
    val iverksettingRepository = mockk<IverksettingRepository>()
    val iverksettResultatService = mockk<IverksettResultatService>()
    val behandlingId: UUID = UUID.randomUUID()
    private val iverksettMotOppdragTask =
        IverksettMotOppdragTask(
            iverksettingRepository = iverksettingRepository,
            oppdragClient = oppdragClient,
            taskService = taskService,
            iverksettResultatService = iverksettResultatService,
            mockFeatureToggleService(),
        )

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettDto(behandlingId).toDomain())
    }

    @Test
    internal fun `skal sende utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } returns "abc"
        every { iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, any()) } returns Unit
        every { iverksettResultatService.hentTilkjentYtelse(any<UUID>()) } returns null
        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) { iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, any()) }
        assertThat(oppdragSlot.captured.fagSystem).isEqualTo("EFOG")
        assertThat(oppdragSlot.captured.kodeEndring).isEqualTo(Utbetalingsoppdrag.KodeEndring.NY)
    }

    @Test
    internal fun `skal ikke iverksette utbetaling til oppdrag når det ikke er noen utbetalinger`() {
        every { iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, any()) } just runs
        every { iverksettResultatService.hentTilkjentYtelse(any<UUID>()) } returns null
        every { iverksettingRepository.findByIdOrThrow(any()) }
            .returns(lagIverksett(opprettIverksettDto(behandlingId, andelsbeløp = 0).toDomain()))
        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
        verify(exactly = 1) { iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, any()) }
        verify(exactly = 0) { oppdragClient.iverksettOppdrag(any()) }
    }

    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val taskSlot = slot<Task>()
        val task = Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties())
        every { taskService.save(capture(taskSlot)) } returns task
        iverksettMotOppdragTask.onCompletion(task)
        assertThat(taskSlot.captured.payload).isEqualTo(behandlingId.toString())
        assertThat(taskSlot.captured.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
    }
}
