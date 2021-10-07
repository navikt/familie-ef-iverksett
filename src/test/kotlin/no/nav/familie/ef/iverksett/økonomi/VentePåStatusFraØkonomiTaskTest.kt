package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class VentePåStatusFraØkonomiTaskTest {

    private val oppdragClient = mockk<OppdragClient>()
    val iverksettingRepository = mockk<IverksettingRepository>()
    val taskRepository = mockk<TaskRepository>()
    val tilstandRepository = mockk<TilstandRepository>()
    val behandlingId: UUID = UUID.randomUUID()
    private val iverksettingService = IverksettingService(taskRepository = taskRepository,
                                                          oppdragClient = oppdragClient,
                                                          iverksettingRepository = iverksettingRepository,
                                                          tilstandRepository = tilstandRepository)

    private val ventePåStatusFraØkonomiTask =
            VentePåStatusFraØkonomiTask(iverksettingRepository, iverksettingService, taskRepository, tilstandRepository)

    @Test
    internal fun `kjør doTask for VentePåStatusFraØkonomiTaskhvis, forvent ingen unntak`() {
        val oppdragResultatSlot = slot<OppdragResultat>()

        every { oppdragClient.hentStatus(any()) } returns OppdragStatusMedMelding(OppdragStatus.KVITTERT_OK, "OK")
        every { iverksettingRepository.hent(any()) } returns opprettIverksettDto(behandlingId).toDomain()
        every { tilstandRepository.oppdaterOppdragResultat(behandlingId, any()) } returns Unit

        ventePåStatusFraØkonomiTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
        verify(exactly = 1) { tilstandRepository.oppdaterOppdragResultat(behandlingId, capture(oppdragResultatSlot)) }
        assertThat(oppdragResultatSlot.captured.oppdragStatus).isEqualTo(OppdragStatus.KVITTERT_OK)
    }
}