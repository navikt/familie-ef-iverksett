package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.august
import no.nav.familie.ef.iverksett.februar
import no.nav.familie.ef.iverksett.januar
import no.nav.familie.ef.iverksett.juli
import no.nav.familie.ef.iverksett.posteringer
import no.nav.familie.ef.iverksett.tilDetaljertSimuleringsresultat
import no.nav.familie.ef.iverksett.tilSimuleringMottakere
import no.nav.familie.ef.iverksett.tilSimuleringsperioder
import no.nav.familie.kontrakter.felles.simulering.PosteringType
import no.nav.familie.kontrakter.felles.simulering.PosteringType.FEILUTBETALING
import no.nav.familie.kontrakter.felles.simulering.PosteringType.YTELSE
import no.nav.familie.kontrakter.felles.simulering.SimulertPostering
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SimuleringUtilTest {

    @Test
    internal fun `skal ikke mappe simuleringsdata for forskuddskatt, motp, justering og trekk `() {

        val posteringer =
                posteringer(januar(2020), posteringstype = PosteringType.MOTP) +
                posteringer(januar(2020), posteringstype = PosteringType.FORSKUDSSKATT)+
                posteringer(januar(2020), posteringstype = PosteringType.JUSTERING)+
                posteringer(januar(2020), posteringstype = PosteringType.TREKK)

        val simuleringsoppsummering = lagSimuleringsoppsummering(
                posteringer.tilDetaljertSimuleringsresultat(),
                1.januar(2021))

        assertThat(simuleringsoppsummering.perioder).isEmpty()
        assertThat(simuleringsoppsummering.etterbetaling).isZero
        assertThat(simuleringsoppsummering.feilutbetaling).isZero
    }

    @Test
    internal fun `skal mappe simuleringsdata for enkel ytelse`() {
        val posteringer =
                posteringer(januar(2020), posteringstype = YTELSE, antallMåneder = 36, beløp = 5_000)

        val simuleringsoppsummering =
                lagSimuleringsoppsummering(
                        posteringer.tilDetaljertSimuleringsresultat(),
                        1.januar(2021))

        val posteringerGruppert = simuleringsoppsummering.perioder
        assertThat(posteringerGruppert).hasSize(36)
        assertThat(posteringerGruppert.sumOf { it.feilutbetaling }).isZero
        assertThat(posteringerGruppert.sumOf { it.resultat.toInt() }).isEqualTo(5000 * 36)
        assertThat(posteringerGruppert.first().nyttBeløp.toInt()).isEqualTo(5000)
        assertThat(posteringerGruppert.last().nyttBeløp.toInt()).isEqualTo(5000)
        assertThat(posteringerGruppert.first().fom).isEqualTo(1.januar(2020))
        assertThat(posteringerGruppert.last().fom).isEqualTo(1.januar(2020).plusMonths(35))
        assertThat(simuleringsoppsummering.etterbetaling.toInt()).isEqualTo(5000 * 12)
        assertThat(simuleringsoppsummering.feilutbetaling).isZero
        assertThat(simuleringsoppsummering.fom).isEqualTo(1.januar(2020))
        assertThat(simuleringsoppsummering.forfallsdatoNestePeriode).isEqualTo(januar(2021).atEndOfMonth())
    }

    @Test
    internal fun `skal mappe simuleringsdata for ytelse hvor bruker har fått for mye i 6 måneder`() {
        val posteringer =
                posteringer(januar(2020), posteringstype = YTELSE, antallMåneder = 6, beløp = 5_000) +
                posteringer(juli(2020), posteringstype = FEILUTBETALING, antallMåneder = 6, beløp = 2_000) +
                posteringer(juli(2020), posteringstype = YTELSE, antallMåneder = 6, beløp = -5000) +
                posteringer(juli(2020), posteringstype = YTELSE, antallMåneder = 7, beløp = 3000) +
                posteringer(juli(2020), posteringstype = YTELSE, antallMåneder = 6, beløp = 2000)

        val simuleringsoppsummering =
                lagSimuleringsoppsummering(
                       posteringer.tilDetaljertSimuleringsresultat(),
                       1.januar(2021))

        val posteringerGruppert = simuleringsoppsummering.perioder

        assertThat(posteringerGruppert.size).isEqualTo(13)
        assertThat(posteringerGruppert.sumOf { it.feilutbetaling.toInt() }).isEqualTo(2_000 * 6)
        assertThat(posteringerGruppert.sumOf { it.nyttBeløp.toInt() }).isEqualTo(3000 + 5_000 * 12 - 2_000 * 6)
        assertThat(posteringerGruppert.sumOf { it.resultat.toInt() }).isEqualTo(3000 + 5_000 * 6 - 2_000 * 6)
        assertThat(posteringerGruppert.first().nyttBeløp.toInt()).isEqualTo(5_000)
        assertThat(posteringerGruppert.last().nyttBeløp.toInt()).isEqualTo(3_000)
        assertThat(posteringerGruppert.first().fom).isEqualTo(1.januar(2020))
        assertThat(posteringerGruppert.last().fom).isEqualTo(1.januar(2021))
        assertThat(simuleringsoppsummering.etterbetaling.toInt()).isEqualTo(5_000 * 6)
        assertThat(simuleringsoppsummering.feilutbetaling.toInt()).isEqualTo(2_000 * 6)
        assertThat(simuleringsoppsummering.fom).isEqualTo(1.januar(2020))
        assertThat(simuleringsoppsummering.forfallsdatoNestePeriode).isEqualTo(januar(2021).atEndOfMonth())
    }

    @Test
    fun `skal lage tom liste av simuleringsperioder`() {
        assertThat(emptyList<SimulertPostering>().tilSimuleringsperioder())
                .isEmpty()
    }

    @Test
    fun `skal gruppere og sortere på fom-dato`() {

        val simuleringsperioder =
                (posteringer(januar(2021), 2, 3_000, YTELSE) +
                 posteringer(januar(2021), 3, 5_000, YTELSE) +
                 posteringer(februar(2021), 3, 2_000, YTELSE)
                ).tilSimuleringsperioder()

        assertThat(simuleringsperioder.size).isEqualTo(4)
        assertThat(simuleringsperioder[0].nyttBeløp.toInt()).isEqualTo(8_000)
        assertThat(simuleringsperioder[1].nyttBeløp.toInt()).isEqualTo(10_000)
        assertThat(simuleringsperioder[2].nyttBeløp.toInt()).isEqualTo(7_000)
        assertThat(simuleringsperioder[3].nyttBeløp.toInt()).isEqualTo(2_000)
    }

    @Test
    fun `Test henting av 'nytt beløp ', 'tidligere utbetalt ' og 'resultat ' for simuleringsperiode uten feilutbetaling`() {
        val posteringer =
                posteringer(juli(2021), beløp = 100, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = 100, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -99, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -99, posteringstype = YTELSE)

        val simuleringsperioder = grupperPosteringerEtterDato(
                posteringer.tilSimuleringMottakere())

        Assertions.assertEquals(1, simuleringsperioder.size)
        Assertions.assertEquals(BigDecimal.valueOf(200), simuleringsperioder[0].nyttBeløp)
        Assertions.assertEquals(BigDecimal.valueOf(198), simuleringsperioder[0].tidligereUtbetalt)
        Assertions.assertEquals(BigDecimal.valueOf(2), simuleringsperioder[0].resultat)
    }

    @Test
    fun `Test henting av 'nytt beløp', 'tidligere utbetalt' og 'resultat' for simuleringsperiode med feilutbetaling`() {
        val posteringer =
                posteringer(juli(2021), beløp = 100, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = 100, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -99, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -99, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = 98, posteringstype = FEILUTBETALING) +
                posteringer(juli(2021), beløp = 98, posteringstype = FEILUTBETALING)

        val simuleringsperioder = grupperPosteringerEtterDato(
                posteringer.tilSimuleringMottakere())

        Assertions.assertEquals(1, simuleringsperioder.size)
        Assertions.assertEquals(BigDecimal.valueOf(4), simuleringsperioder[0].nyttBeløp)
        Assertions.assertEquals(BigDecimal.valueOf(198), simuleringsperioder[0].tidligereUtbetalt)
        Assertions.assertEquals(BigDecimal.valueOf(-196), simuleringsperioder[0].resultat)
    }

    @Test
    fun `Test 'nytt beløp', 'tidligere utbetalt' og 'resultat' for simuleringsperiode med reduksjon i feilutbetaling`() {
        val posteringer =
                posteringer(juli(2021), beløp = 100, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = 100, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -99, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -99, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = 98, posteringstype = FEILUTBETALING) +
                posteringer(juli(2021), beløp = -99, posteringstype = FEILUTBETALING)

        val simuleringsperioder = grupperPosteringerEtterDato(
                posteringer.tilSimuleringMottakere())

        Assertions.assertEquals(1, simuleringsperioder.size)

        Assertions.assertEquals(BigDecimal.valueOf(200), simuleringsperioder[0].nyttBeløp)
        Assertions.assertEquals(BigDecimal.valueOf(197), simuleringsperioder[0].tidligereUtbetalt)
        Assertions.assertEquals(BigDecimal.valueOf(1), simuleringsperioder[0].resultat)
    }

    val simulertePosteringerMedNegativFeilutbetaling =
            posteringer(juli(2021), beløp = -500, posteringstype = FEILUTBETALING) +
            posteringer(juli(2021), beløp = -2000, posteringstype = YTELSE) +
            posteringer(juli(2021), beløp = 3000, posteringstype = YTELSE) +
            posteringer(juli(2021), beløp = -500, posteringstype = YTELSE)

    @Test
    fun `Total etterbetaling skal bli summen av ytelsene i periode med negativ feilutbetaling`() {
        val restSimulering = lagSimuleringsoppsummering(
                simulertePosteringerMedNegativFeilutbetaling.tilDetaljertSimuleringsresultat(),
                15.august(2021)
        )

        Assertions.assertEquals(BigDecimal.valueOf(500), restSimulering.etterbetaling)
    }

    @Test
    fun `Total feilutbetaling skal bli 0 i periode med negativ feilutbetaling`() {
        val restSimulering = lagSimuleringsoppsummering(
                simulertePosteringerMedNegativFeilutbetaling.tilDetaljertSimuleringsresultat(),
                15.august(2021)
        )

        Assertions.assertEquals(BigDecimal.valueOf(0), restSimulering.feilutbetaling)
    }

    @Test
    fun `Skal gi 0 etterbetaling og sum feilutbetaling ved positiv feilutbetaling`() {
        val posteringer =
                posteringer(juli(2021), beløp = 500, posteringstype = FEILUTBETALING) +
                posteringer(juli(2021), beløp = -2000, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = 3000, posteringstype = YTELSE) +
                posteringer(juli(2021), beløp = -500, posteringstype = YTELSE)

        val restSimulering = lagSimuleringsoppsummering(
                posteringer.tilDetaljertSimuleringsresultat(),
                15.august(2021)
        )

        Assertions.assertEquals(BigDecimal.valueOf(0), restSimulering.etterbetaling)
        Assertions.assertEquals(BigDecimal.valueOf(500), restSimulering.feilutbetaling)
    }

}