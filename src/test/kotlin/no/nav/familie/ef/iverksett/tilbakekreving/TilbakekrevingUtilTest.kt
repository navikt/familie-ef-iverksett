package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.medFeilutbetaling
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.util.opprettTilbakekrevingMedVarsel
import no.nav.familie.kontrakter.felles.Datoperiode
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class TilbakekrevingUtilTest {

    private val fom: LocalDate = LocalDate.of(2021, 1, 1)
    private val tom: LocalDate = LocalDate.of(2021, 12, 31)
    val perioder = listOf(Datoperiode(fom = fom, tom = tom))

    @Test
    fun `uendret tilbakekreving med varsel skal opprettholdes i iverksett`() {
        val feilutbetaling = BigDecimal.TEN
        val tilbakekrevingsdetaljer = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
            tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(feilutbetaling, perioder),
        )
        val iverksett = opprettIverksettOvergangsstønad(UUID.randomUUID(), tilbakekreving = tilbakekrevingsdetaljer)

        val beriketSimuleringsresultat = beriketSimuleringsresultat(feilutbetaling, fom, tom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isEqualTo(tilbakekrevingsdetaljer)
    }

    @Test
    fun `oppdaterVarsel skal bare ta med perioder som har feilutbetalinger i varselet - og slå de sammen`() {
        val simuleringsoppsummering =
            Simuleringsoppsummering(
                perioder = listOf(
                    Simuleringsperiode(
                        fom = LocalDate.of(2021, 12, 1),
                        tom = LocalDate.of(2021, 12, 31),
                        forfallsdato = LocalDate.of(2022, 3, 8),
                        nyttBeløp = BigDecimal(11295.0),
                        tidligereUtbetalt = BigDecimal(11295.0),
                        resultat = BigDecimal(0.0),
                        feilutbetaling = BigDecimal(0),
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2022, 1, 1),
                        tom = LocalDate.of(2022, 1, 31),
                        forfallsdato = LocalDate.of(2022, 3, 8),
                        nyttBeløp = BigDecimal(8745.0),
                        tidligereUtbetalt = BigDecimal(11295.0),
                        resultat = BigDecimal(-2550.0),
                        feilutbetaling = BigDecimal(2550.0),
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2022, 2, 1),
                        tom = LocalDate.of(2022, 2, 28),
                        forfallsdato = LocalDate.of(2022, 3, 8),
                        nyttBeløp = BigDecimal(8745.0),
                        tidligereUtbetalt = BigDecimal(11295.0),
                        resultat = BigDecimal(-2550.0),
                        feilutbetaling = BigDecimal(2550.0),
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2022, 3, 1),
                        tom = LocalDate.of(2022, 3, 31),
                        forfallsdato = LocalDate.of(2022, 3, 17),
                        nyttBeløp = BigDecimal(4620.0),
                        tidligereUtbetalt = BigDecimal(0),
                        resultat = BigDecimal(4620.0),
                        feilutbetaling = BigDecimal(0),
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2022, 4, 1),
                        tom = LocalDate.of(2022, 4, 30),
                        forfallsdato = LocalDate.of(2022, 4, 13),
                        nyttBeløp = BigDecimal(4620.0),
                        tidligereUtbetalt = BigDecimal(0),
                        resultat = BigDecimal(4620.0),
                        feilutbetaling = BigDecimal(0),
                    ),
                ),
                fomDatoNestePeriode = LocalDate.of(2022, 3, 1),
                etterbetaling = BigDecimal(0.0),
                feilutbetaling = BigDecimal(5100.0),
                fom = LocalDate.of(2021, 12, 1),
                tomDatoNestePeriode = LocalDate.of(2022, 3, 31),
                forfallsdatoNestePeriode = LocalDate.of(2022, 3, 17),
                tidSimuleringHentet = LocalDate.of(2022, 3, 10),
                tomSisteUtbetaling = LocalDate.of(2022, 2, 28),
            )
        val tilbakekrevingsdetaljer =
            Tilbakekrevingsdetaljer(
                Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                TilbakekrevingMedVarsel("varseltekst", BigDecimal.ZERO, listOf()),
            )

        val varsel = tilbakekrevingsdetaljer.oppdaterVarsel(simuleringsoppsummering)

        assertThat(varsel.tilbakekrevingMedVarsel?.perioder?.size).isEqualTo(1)
        assertThat(varsel.tilbakekrevingMedVarsel?.perioder?.first()?.fom).isEqualTo(LocalDate.of(2022, 1, 1))
        assertThat(varsel.tilbakekrevingMedVarsel?.perioder?.first()?.tom).isEqualTo(LocalDate.of(2022, 2, 28))
        assertThat(varsel.tilbakekrevingMedVarsel?.sumFeilutbetaling).isEqualTo(BigDecimal(5100.0))
    }

    @Test
    fun `endret feilutbetaling i iverksett skal tas hensyn til`() {
        val originalTilbakekreving = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
            tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(BigDecimal.TEN, perioder),
        )
        val iverksett = opprettIverksettOvergangsstønad(UUID.randomUUID(), tilbakekreving = originalTilbakekreving)

        val nyFom = fom.minusMonths(1)
        val nyTom = tom.plusMonths(1)
        val nyPeriode = Datoperiode(nyFom, nyTom)
        val beriketSimuleringsresultat = beriketSimuleringsresultat(BigDecimal.ONE, nyFom, nyTom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isNotEqualTo(originalTilbakekreving)
        assertThat(nyTilbakekreving).isEqualTo(originalTilbakekreving.medFeilutbetaling(BigDecimal.ONE, nyPeriode))
    }

    @Test
    fun `ingen feilutbetaling i iverksett skal fjerne tilbakekreving`() {
        val originalTilbakekreving = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
            tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(BigDecimal.TEN, perioder),
        )
        val iverksett = opprettIverksettOvergangsstønad(UUID.randomUUID(), tilbakekreving = originalTilbakekreving)

        val beriketSimuleringsresultat = beriketSimuleringsresultat(BigDecimal.ZERO, fom, tom)

        val nyTilbakekreving = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat).vedtak.tilbakekreving

        assertThat(nyTilbakekreving).isNull()
    }
}
