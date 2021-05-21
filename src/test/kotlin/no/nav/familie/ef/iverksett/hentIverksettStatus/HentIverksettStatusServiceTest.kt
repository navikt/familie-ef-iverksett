package no.nav.familie.ef.iverksett.hentIverksettStatus

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.domene.IverksettStatus
import no.nav.familie.ef.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.tilstand.hent.HentTilstandService
import no.nav.familie.ef.iverksett.util.IverksettResultatMockBuilder
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class HentIverksettStatusServiceTest {

    val hentTilstandService: HentTilstandService = mockk<HentTilstandService>()

    private var hentIverksettStatusService: HentIverksettStatusService = HentIverksettStatusService(hentTilstandService)


    @Test
    fun `la IverksettResultat ha felt kun satt for tilkjent ytelse, forvent status SENDT_TIL_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { hentTilstandService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .build(behandlingsId, tilkjentYtelse)

        val status = hentIverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.SENDT_TIL_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha felt satt for tilkjent ytelse, oppdrag, og oppdragsresultat, forvent status FEILET_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { hentTilstandService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL))
            .build(behandlingsId, tilkjentYtelse)

        val status = hentIverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.FEILET_MOT_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha felt satt for tilkjent ytelse, oppdrag med kvittert_ok, forvent status OK_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { hentTilstandService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .build(behandlingsId, tilkjentYtelse)

        val status = hentIverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.OK_MOT_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha felt satt for journalføring, forvent status JOURNALFØRT`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { hentTilstandService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .journalPostResultat()
            .build(behandlingsId, tilkjentYtelse)

        val status = hentIverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.JOURNALFØRT)
    }

    @Test
    fun `la IverksettResultat ha felt for vedktasbrev ulik null, forvent status DISTRIBUERT`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { hentTilstandService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .journalPostResultat()
            .vedtaksbrevResultat(behandlingsId).build(behandlingsId, tilkjentYtelse)

        val status = hentIverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.DISTRIBUERT)
    }


}