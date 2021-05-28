package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettDbUtil
import no.nav.familie.ef.iverksett.iverksettingstatus.status.tilstand.TilstandDbUtil
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class IverksettMotOppdragTaskTest {


    val oppdragClient = mockk<OppdragClient>()
    val taskRepository = mockk<TaskRepository>()
    val iverksettDbUtil = mockk<IverksettDbUtil>()
    val tilstandDbUtil = mockk<TilstandDbUtil>()
    val behandlingId = UUID.randomUUID()
    val iverksettMotOppdragTask =
            IverksettMotOppdragTask(
                    iverksettDbUtil = iverksettDbUtil,
                    oppdragClient = oppdragClient,
                    taskRepository = taskRepository,
                    tilstandDbUtil = tilstandDbUtil
            )

    @BeforeEach
    internal fun setUp() {
        every { iverksettDbUtil.hentIverksett(any()) } returns opprettIverksettDto(behandlingId).toDomain()
    }

    @Test
    internal fun `skal sende utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } returns "abc"
        every { tilstandDbUtil.lagreTilkjentYtelseForUtbetaling(behandlingId, any()) } returns Unit
        every { tilstandDbUtil.hentTilkjentYtelse(any()) } returns null
        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) { tilstandDbUtil.lagreTilkjentYtelseForUtbetaling(behandlingId, any()) }
        assertThat(oppdragSlot.captured.fagSystem).isEqualTo("EFOG")
        assertThat(oppdragSlot.captured.kodeEndring).isEqualTo(Utbetalingsoppdrag.KodeEndring.NY)
    }

    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val taskSlot = slot<Task>()
        val task = Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties())
        every { taskRepository.save(capture(taskSlot)) } returns task
        iverksettMotOppdragTask.onCompletion(task)
        assertThat(taskSlot.captured.payload).isEqualTo(behandlingId.toString())
        assertThat(taskSlot.captured.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
    }
}