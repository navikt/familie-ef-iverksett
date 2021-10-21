package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal

private val TILBAKEKREVING_UTEN_VARSEL =
        Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
                tilbakekrevingMedVarsel = null)

fun Iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat: BeriketSimuleringsresultat): Iverksett {

    val tilbakekreving = this.vedtak.tilbakekreving
    val simuleringsoppsummering = beriketSimuleringsresultat.oppsummering

    var nyTilbakekreving = tilbakekreving
    if (tilbakekreving == null && simuleringsoppsummering.feilutbetaling > BigDecimal.ZERO)
        nyTilbakekreving = TILBAKEKREVING_UTEN_VARSEL
    else if (harAvvikIVarsel(tilbakekreving, simuleringsoppsummering))
        nyTilbakekreving = tilbakekreving!!.oppdaterVarsel(simuleringsoppsummering)

    return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
}

private fun harAvvikIVarsel(tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer?,
                            simuleringsoppsummering: Simuleringsoppsummering): Boolean {

    val varsel = tilbakekrevingsdetaljer?.tilbakekrevingMedVarsel
    // Fanger ikke opp endringer i perioder hvis feilutbetalingen er lik
    // MEN bør ikke endre perioder i tilfelle klient har konsolidert dem
    return varsel != null && simuleringsoppsummering.feilutbetaling != varsel.sumFeilutbetaling
}

fun Iverksett.skalTilbakekreves() =
        this.vedtak.tilbakekreving != null &&
        this.vedtak.tilbakekreving.tilbakekrevingsvalg != Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING

fun Tilbakekrevingsdetaljer.oppdaterVarsel(simuleringsoppsummering: Simuleringsoppsummering) =
        this.copy(
                tilbakekrevingMedVarsel = this.tilbakekrevingMedVarsel?.copy(
                        sumFeilutbetaling = simuleringsoppsummering.feilutbetaling,
                        perioder = simuleringsoppsummering.perioder.map { Periode(it.fom, it.tom) }))
