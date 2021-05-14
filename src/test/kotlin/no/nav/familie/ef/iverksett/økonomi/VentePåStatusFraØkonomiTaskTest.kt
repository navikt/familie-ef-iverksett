package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Test
import java.util.*

internal class VentePåStatusFraØkonomiTaskTest {

    val oppdragClient = mockk<OppdragClient>()
    val hentIverksettService = mockk<HentIverksettService>()
    val taskRepository = mockk<TaskRepository>()
    val behandlingId = UUID.randomUUID()

    val ventePåStatusFraØkonomiTask = VentePåStatusFraØkonomiTask(hentIverksettService, oppdragClient, taskRepository)

    @Test
    internal fun `kjør doTask for VentePåStatusFraØkonomiTaskhvis, forvent ingen unntak`() {
        every { oppdragClient.hentStatus(any()) } returns OppdragStatus.KVITTERT_OK
        every { hentIverksettService.hentIverksett(any()) } returns opprettIverksettDto(behandlingId).toDomain()
        ventePåStatusFraØkonomiTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))
    }
}