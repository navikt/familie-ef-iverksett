package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class IverksettMotOppdragTaskTest {


    val oppdragClient = mockk<OppdragClient>()
    val taskRepository = mockk<TaskRepository>()
    val hentIverksettService = mockk<HentIverksettService>()
    val lagreTilstandService = mockk<LagreTilstandService>()
    val behandlingId = UUID.randomUUID()
    val iverksettMotOppdragTask =
        IverksettMotOppdragTask(
            hentIverksettService = hentIverksettService,
            oppdragClient = oppdragClient,
            taskRepository = taskRepository,
            lagreTilstandService = lagreTilstandService
        )

    @BeforeEach
    internal fun setUp() {
        every { hentIverksettService.hentIverksett(any()) } returns opprettIverksettDto(behandlingId).toDomain()
    }

    @Test
    internal fun `skal sende utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } returns "abc"
        every { lagreTilstandService.lagreTilkjentYtelseForUtbetaling(behandlingId, any()) } returns Unit

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) { lagreTilstandService.lagreTilkjentYtelseForUtbetaling(behandlingId, any()) }
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