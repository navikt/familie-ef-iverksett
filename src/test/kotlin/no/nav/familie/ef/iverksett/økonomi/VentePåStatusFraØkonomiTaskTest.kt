package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.IverksettingDbUtil
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandDbUtil
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class VentePåStatusFraØkonomiTaskTest {

    val oppdragClient = mockk<OppdragClient>()
    val iverksettDbUtil = mockk<IverksettingDbUtil>()
    val taskRepository = mockk<TaskRepository>()
    val lagreTilstandService = mockk<TilstandDbUtil>()
    val behandlingId = UUID.randomUUID()

    val ventePåStatusFraØkonomiTask =
            VentePåStatusFraØkonomiTask(iverksettDbUtil, oppdragClient, taskRepository, lagreTilstandService)

    @Test
    internal fun `kjør doTask for VentePåStatusFraØkonomiTaskhvis, forvent ingen unntak`() {
        val oppdragResultatSlot = slot<OppdragResultat>()

        every { oppdragClient.hentStatus(any()) } returns OppdragStatus.KVITTERT_OK
        every { iverksettDbUtil.hentIverksett(any()) } returns opprettIverksettDto(behandlingId).toDomain()
        every { lagreTilstandService.lagreOppdragResultat(behandlingId, any()) } returns Unit

        ventePåStatusFraØkonomiTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
        verify(exactly = 1) { lagreTilstandService.lagreOppdragResultat(behandlingId, capture(oppdragResultatSlot)) }
        assertThat(oppdragResultatSlot.captured.oppdragStatus).isEqualTo(OppdragStatus.KVITTERT_OK)
    }
}