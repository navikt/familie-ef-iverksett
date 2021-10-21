package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
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

    val nyTilbakekreving: Tilbakekrevingsdetaljer?
    if (tilbakekreving == null && simuleringsoppsummering.feilutbetaling > BigDecimal.ZERO)
        nyTilbakekreving = TILBAKEKREVING_UTEN_VARSEL
    else if (tilbakekreving != null && simuleringsoppsummering.feilutbetaling <= BigDecimal.ZERO)
        nyTilbakekreving = null
    else if (harAvvikIVarsel(tilbakekreving, simuleringsoppsummering))
        nyTilbakekreving = tilbakekreving?.oppdaterVarsel(simuleringsoppsummering)
    else
        nyTilbakekreving = tilbakekreving

    return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
}

private fun harAvvikIVarsel(tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer?,
                            simuleringsoppsummering: Simuleringsoppsummering): Boolean {
    // Sjekker ikke periodene fordi de kan være ulikt konsolidert
    // Er jo et spørsmål om evt konsolideringslogikk heller burde ligge her i ef-iverksett i stedet for ef-sak
    // slik at de kan sammenliknes konsistent
    val varsel = tilbakekrevingsdetaljer?.tilbakekrevingMedVarsel
    return varsel != null && simuleringsoppsummering.feilutbetaling != varsel.sumFeilutbetaling
}

fun Tilbakekrevingsdetaljer?.skalTilbakekreves(): Boolean {
    return this != null
           && this.tilbakekrevingsvalg != Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING
}

fun Tilbakekrevingsdetaljer.oppdaterVarsel(simuleringsoppsummering: Simuleringsoppsummering): Tilbakekrevingsdetaljer? {

    return this.copy(
            tilbakekrevingMedVarsel = this.tilbakekrevingMedVarsel?.copy(
                    sumFeilutbetaling = simuleringsoppsummering.feilutbetaling,
                    // Kjøres ikke konsolidering her. Det (kan hende at det) gjøres i ef-sak
                    perioder = simuleringsoppsummering.perioder.map { Periode(it.fom, it.tom) }))

}
