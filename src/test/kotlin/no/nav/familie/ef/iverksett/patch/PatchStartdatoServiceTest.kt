package no.nav.familie.ef.iverksett.patch

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

internal class PatchStartdatoServiceTest : ServerTest() {


    @Autowired private lateinit var iverksettingRepository: IverksettingRepository
    @Autowired private lateinit var tilstandRepository: TilstandRepository
    @Autowired private lateinit var patchStartdatoService: PatchStartdatoService

    @Test
    internal fun `skal sette startdato`() {
        val behandlingId = UUID.randomUUID()
        val startdato = LocalDate.of(2020, 1, 1)

        val iverksett = opprettIverksett(behandlingId, null, emptyList(), null)
        iverksettingRepository.lagre(behandlingId, iverksett, null)
        tilstandRepository.opprettTomtResultat(behandlingId)
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, TilkjentYtelse(andelerTilkjentYtelse = emptyList(), startdato = null))

        patchStartdatoService.patch(behandlingId, startdato, true)

        assertThat(iverksettingRepository.hent(behandlingId).vedtak.tilkjentYtelse!!.startdato).isEqualTo(startdato)
        assertThat(tilstandRepository.hentTilkjentYtelse(behandlingId)!!.startdato).isEqualTo(startdato)
    }

    @Test
    internal fun `oppdaterer ikke når det ikke finnes en iverksetting for valgt behandling`() {
        val behandlingId = UUID.randomUUID()
        val startdato = LocalDate.of(2020, 1, 1)
        patchStartdatoService.patch(behandlingId, startdato, true)
    }

}