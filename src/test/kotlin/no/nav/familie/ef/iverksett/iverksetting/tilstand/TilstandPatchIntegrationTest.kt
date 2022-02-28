package no.nav.familie.ef.iverksett.iverksetting.tilstand

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettIverksett
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class TilstandPatchIntegrationTest : ServerTest() {

    @Autowired private lateinit var tilstandPatch: TilstandPatch
    @Autowired private lateinit var tilstandRepository: TilstandRepository
    @Autowired private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    internal fun `har tom liste med andeler`() {
        val behandlingId = UUID.randomUUID()
        iverksettingRepository.lagre(behandlingId, opprettIverksett(behandlingId, null), null)
        tilstandRepository.opprettTomtResultat(behandlingId)
        val tilkjentYtelse = TilkjentYtelse(utbetalingsoppdrag = null, startdato = null, andelerTilkjentYtelse = emptyList())
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        tilstandPatch.oppdaterSisteAndelIKjede(true)
    }
}