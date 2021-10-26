package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.medFeilutbetaling
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.util.opprettTilbakekrevingMedVarsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class TilbakekrevingUtilTest {

    val fom = LocalDate.of(2021, 1, 1)
    val tom = LocalDate.of(2021, 12, 31)
    val perioder = listOf(Periode(fom = fom, tom = tom))

    @Test
    fun `uendret tilbakekreving med varsel skal opprettholdes i iverksett`() {

        val feilutbetaling = BigDecimal.TEN
        val tilbakekrevingsdetaljer = Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(feilutbetaling, perioder)
        )
        val iverksett = opprettIverksett(UUID.randomUUID(), tilbakekreving = tilbakekrevingsdetaljer)

        val beriketSimuleringsresultat = beriketSimuleringsresultat(feilutbetaling, fom, tom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isEqualTo(tilbakekrevingsdetaljer)
    }

    @Test
    fun `endret feilutbetaling i iverksett skal tas hensyn til`() {

        val originalTilbakekreving = Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(BigDecimal.TEN, perioder)
        )
        val iverksett = opprettIverksett(UUID.randomUUID(), tilbakekreving = originalTilbakekreving)

        val nyFom = fom.minusMonths(1)
        val nyTom = tom.plusMonths(1)
        val nyPeriode = Periode(nyFom, nyTom)
        val beriketSimuleringsresultat = beriketSimuleringsresultat(BigDecimal.ONE, nyFom, nyTom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isNotEqualTo(originalTilbakekreving)
        assertThat(nyTilbakekreving).isEqualTo(originalTilbakekreving.medFeilutbetaling(BigDecimal.ONE, nyPeriode))
    }

    @Test
    fun `ingen feilutbetaling i iverksett skal fjerne tilbakekreving`() {

        val originalTilbakekreving = Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(BigDecimal.TEN, perioder)
        )
        val iverksett = opprettIverksett(UUID.randomUUID(), tilbakekreving = originalTilbakekreving)

        val beriketSimuleringsresultat = beriketSimuleringsresultat(BigDecimal.ZERO, fom, tom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isNull()
    }

    @Test
    fun `oppdagelse av feilutbetaling i iverksett skal legge til tilbakekreving uten varsel`() {

        val iverksett = opprettIverksett(UUID.randomUUID(), tilbakekreving = null)

        val beriketSimuleringsresultat = beriketSimuleringsresultat(BigDecimal.TEN, fom, tom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isNotNull
        assertThat(nyTilbakekreving!!.tilbakekrevingsvalg).isEqualTo(Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL)
        assertThat(nyTilbakekreving.tilbakekrevingMedVarsel).isNull()
    }
}