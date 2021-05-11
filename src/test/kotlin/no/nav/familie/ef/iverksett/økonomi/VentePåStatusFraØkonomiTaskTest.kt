package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.*

internal class VentePåStatusFraØkonomiTaskTest {

    val oppdragClient = mockk<OppdragClient>()
    val hentIverksettService = mockk<HentIverksettService>()
    val behandlingId = UUID.randomUUID().toString()

    val ventePåStatusFraØkonomiTask = VentePåStatusFraØkonomiTask(hentIverksettService, oppdragClient)

    @Test
    internal fun `kjør doTask for VentePåStatusFraØkonomiTaskhvis, forvent ingen unntak`() {
        every { oppdragClient.hentStatus(any()) } returns OppdragStatus.KVITTERT_OK
        ventePåStatusFraØkonomiTask.doTask(Task(IverksettMotOppdragTask.TYPE, behandlingId, Properties()))
    }
}