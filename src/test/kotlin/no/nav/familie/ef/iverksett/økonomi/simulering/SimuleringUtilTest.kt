package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.august
import no.nav.familie.ef.iverksett.februar
import no.nav.familie.ef.iverksett.januar
import no.nav.familie.ef.iverksett.juli
import no.nav.familie.ef.iverksett.mai
import no.nav.familie.ef.iverksett.november
import no.nav.familie.ef.iverksett.posteringer
import no.nav.familie.ef.iverksett.september
import no.nav.familie.ef.iverksett.tilDetaljertSimuleringsresultat
import no.nav.familie.ef.iverksett.tilSimuleringMottakere
import no.nav.familie.ef.iverksett.tilSimuleringsperioder
import no.nav.familie.kontrakter.felles.simulering.BetalingType.DEBIT
import no.nav.familie.kontrakter.felles.simulering.BetalingType.KREDIT
import no.nav.familie.kontrakter.felles.simulering.PosteringType
import no.nav.familie.kontrakter.felles.simulering.PosteringType.FEILUTBETALING
import no.nav.familie.kontrakter.felles.simulering.PosteringType.MOTP
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
                posteringer(januar(2020), posteringstype = PosteringType.FORSKUDSSKATT) +
                posteringer(januar(2020), posteringstype = PosteringType.JUSTERING) +
                posteringer(januar(2020), posteringstype = PosteringType.TREKK)

        val simuleringsoppsummering =
            lagSimuleringsoppsummering(
                posteringer.tilDetaljertSimuleringsresultat(),
                1.januar(2021),
            )

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
                1.januar(2021),
            )

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
                1.januar(2021),
            )

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
            (
                posteringer(januar(2021), 2, 3_000, YTELSE) +
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

        val simuleringsperioder =
            grupperPosteringerEtterDato(
                posteringer.tilSimuleringMottakere(),
            )

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

        val simuleringsperioder =
            grupperPosteringerEtterDato(
                posteringer.tilSimuleringMottakere(),
            )

        Assertions.assertEquals(1, simuleringsperioder.size)
        Assertions.assertEquals(BigDecimal.valueOf(4), simuleringsperioder[0].nyttBeløp)
        Assertions.assertEquals(BigDecimal.valueOf(198), simuleringsperioder[0].tidligereUtbetalt)
        Assertions.assertEquals(BigDecimal.valueOf(-196), simuleringsperioder[0].resultat)
    }

    val simulertePosteringerMedNegativFeilutbetaling =
        posteringer(juli(2021), beløp = -500, posteringstype = FEILUTBETALING) +
            posteringer(juli(2021), beløp = -2000, posteringstype = YTELSE) +
            posteringer(juli(2021), beløp = 3000, posteringstype = YTELSE) +
            posteringer(juli(2021), beløp = -500, posteringstype = YTELSE)

    @Test
    fun `Total etterbetaling skal bli summen av ytelsene i periode med negativ feilutbetaling`() {
        val restSimulering =
            lagSimuleringsoppsummering(
                simulertePosteringerMedNegativFeilutbetaling.tilDetaljertSimuleringsresultat(),
                15.august(2021),
            )

        Assertions.assertEquals(BigDecimal.valueOf(500), restSimulering.etterbetaling)
    }

    @Test
    fun `Total feilutbetaling skal bli 0 i periode med negativ feilutbetaling`() {
        val restSimulering =
            lagSimuleringsoppsummering(
                simulertePosteringerMedNegativFeilutbetaling.tilDetaljertSimuleringsresultat(),
                15.august(2021),
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

        val restSimulering =
            lagSimuleringsoppsummering(
                posteringer.tilDetaljertSimuleringsresultat(),
                15.august(2021),
            )

        Assertions.assertEquals(BigDecimal.valueOf(0), restSimulering.etterbetaling)
        Assertions.assertEquals(BigDecimal.valueOf(500), restSimulering.feilutbetaling)
    }

    /*
        De neste testene antar at brukeren går gjennom følgende for ÉN periode:
        - Førstegangsbehandling gir ytelse på kr 10 000
        - Revurdering reduserer ytelse fra kr 10 000 til kr 2 000, dvs kr 8 000 feilutbetalt
        - Revurdering øker ytelse fra kr 2 000 til kr 3 000, dvs feilutbetaling reduseres
        - Revurdering øker ytelse fra kr 3 000 tik kr 12 000, dvs feilutbetaling nulles ut, og etterbetaling skjer
     */
    @Test
    fun `ytelse på 10000 korrigert til 2000`() {
        val redusertYtelseTil2000 =
            posteringer(beløp = -10_000, posteringstype = YTELSE, betalingstype = KREDIT) + // Forrige
                posteringer(beløp = 2_000, posteringstype = YTELSE, betalingstype = DEBIT) + // Ny
                posteringer(beløp = 8_000, posteringstype = FEILUTBETALING, betalingstype = DEBIT) + // Feilutbetaling
                posteringer(beløp = -8_000, posteringstype = MOTP, betalingstype = KREDIT) + // "Nuller ut" Feilutbetalingen
                posteringer(beløp = 8_000, posteringstype = YTELSE, betalingstype = DEBIT) // "Nuller ut" forrige og ny

        val simuleringsperioder = grupperPosteringerEtterDato(redusertYtelseTil2000.tilSimuleringMottakere())
        val oppsummering = lagSimuleringsoppsummering(redusertYtelseTil2000.tilDetaljertSimuleringsresultat(), 15.februar(2021))

        assertThat(simuleringsperioder.size).isEqualTo(1)
        assertThat(simuleringsperioder[0].tidligereUtbetalt).isEqualTo(10_000.toBigDecimal())
        assertThat(simuleringsperioder[0].nyttBeløp).isEqualTo(2_000.toBigDecimal())
        assertThat(simuleringsperioder[0].resultat).isEqualTo(-8_000.toBigDecimal())
        assertThat(simuleringsperioder[0].feilutbetaling).isEqualTo(8_000.toBigDecimal())
        assertThat(oppsummering.etterbetaling).isEqualTo(0.toBigDecimal())
    }

    @Test
    fun `ytelse på 2000 korrigert til 3000`() {
        val øktYtelseFra2000Til3000 =
            posteringer(beløp = -2_000, posteringstype = YTELSE, betalingstype = KREDIT) +
                posteringer(beløp = 3_000, posteringstype = YTELSE, betalingstype = DEBIT) +
                posteringer(beløp = -1_000, posteringstype = FEILUTBETALING, betalingstype = KREDIT) + // Reduser feilutbetaling
                posteringer(beløp = 1_000, posteringstype = MOTP, betalingstype = DEBIT) +
                posteringer(beløp = -1_000, posteringstype = YTELSE, betalingstype = KREDIT)

        val simuleringsperioder = grupperPosteringerEtterDato(øktYtelseFra2000Til3000.tilSimuleringMottakere())
        val oppsummering =
            lagSimuleringsoppsummering(øktYtelseFra2000Til3000.tilDetaljertSimuleringsresultat(), 15.februar(2021))

        assertThat(simuleringsperioder.size).isEqualTo(1)
        assertThat(simuleringsperioder[0].tidligereUtbetalt).isEqualTo(2_000.toBigDecimal())
        assertThat(simuleringsperioder[0].nyttBeløp).isEqualTo(3_000.toBigDecimal())
        assertThat(simuleringsperioder[0].resultat).isEqualTo(1_000.toBigDecimal())
        assertThat(simuleringsperioder[0].feilutbetaling).isEqualTo(0.toBigDecimal())
        assertThat(oppsummering.etterbetaling).isEqualTo(0.toBigDecimal())
    }

    @Test
    fun `ytelse på 3000 korrigert til 12000`() {
        val øktYtelseFra3000Til12000 =
            posteringer(beløp = -3_000, posteringstype = YTELSE, betalingstype = KREDIT) +
                posteringer(beløp = 12_000, posteringstype = YTELSE, betalingstype = DEBIT) +
                posteringer(beløp = -7_000, posteringstype = FEILUTBETALING, betalingstype = KREDIT) + // Reduser feilutb
                posteringer(beløp = 7_000, posteringstype = MOTP, betalingstype = DEBIT) +
                posteringer(beløp = -7_000, posteringstype = YTELSE, betalingstype = KREDIT)

        val simuleringsperioder = grupperPosteringerEtterDato(øktYtelseFra3000Til12000.tilSimuleringMottakere())
        val oppsummering =
            lagSimuleringsoppsummering(øktYtelseFra3000Til12000.tilDetaljertSimuleringsresultat(), 15.februar(2021))

        assertThat(simuleringsperioder.size).isEqualTo(1)
        assertThat(simuleringsperioder[0].tidligereUtbetalt).isEqualTo(3_000.toBigDecimal())
        assertThat(simuleringsperioder[0].nyttBeløp).isEqualTo(12_000.toBigDecimal())
        assertThat(simuleringsperioder[0].resultat).isEqualTo(9_000.toBigDecimal())
        assertThat(simuleringsperioder[0].feilutbetaling).isEqualTo(0.toBigDecimal())
        assertThat(oppsummering.etterbetaling).isEqualTo(2_000.toBigDecimal())
    }

    /*
    De neste testene antar at brukeren går gjennom følgende førstegangsbehandling og revurderinger i november 2021:
    2021	Feb	    Mar	    Apr	    Mai	    Jun	    Jul	    Aug	    Sep	    Okt	    Nov
    18/11	17153	17153	17153	18195	18195	18195	18195	18195	18195
    22/11				    17257	17257	17257	17257
    23/11	17341	17341	17341	18382	18382	18382	18382	18382	18382	18382
     */

    @Test
    fun `førstegangsbehandling 18 nov`() {
        val førstegangsbehandling18nov =
            posteringer(februar(2021), 3, 17_153, YTELSE) +
                posteringer(mai(2021), 6, 18_195, YTELSE)

        val oppsummering =
            lagSimuleringsoppsummering(
                førstegangsbehandling18nov.tilDetaljertSimuleringsresultat(),
                18.november(2021),
            )

        assertThat(oppsummering.feilutbetaling).isEqualTo(0.toBigDecimal())
        assertThat(oppsummering.etterbetaling).isEqualTo(160_629.toBigDecimal())
    }

    @Test
    fun `revurdering 22 nov`() {
        val revurering22nov =
            // Forrige ytelse
            posteringer(februar(2021), 3, -17_153, YTELSE) +
                posteringer(mai(2021), 6, -18_195, YTELSE) +
                // Ny ytelse
                posteringer(februar(2021), 3, 17_153, YTELSE) +
                posteringer(mai(2021), 4, 17_257, YTELSE) +
                posteringer(september(2021), 2, 18_195, YTELSE) +
                // Feilutbetaling
                posteringer(mai(2021), 4, 938, FEILUTBETALING) +
                // Motpost feilutbetaling
                posteringer(mai(2021), 4, -938, MOTP) +
                // Teknisk postering
                posteringer(mai(2021), 4, 938, YTELSE)

        val oppsummering =
            lagSimuleringsoppsummering(
                revurering22nov.tilDetaljertSimuleringsresultat(),
                22.november(2021),
            )

        assertThat(oppsummering.feilutbetaling).isEqualTo(3_752.toBigDecimal())
        assertThat(oppsummering.etterbetaling).isEqualTo(0.toBigDecimal())
    }

    @Test
    fun `revurdering 23 nov`() {
        val revurdering23nov =
            // Forrige utelse
            posteringer(februar(2021), 3, -17_153, YTELSE) +
                posteringer(mai(2021), 4, -17_257, YTELSE) +
                posteringer(september(2021), 2, -18_195, YTELSE) +
                // Ny ytelse
                posteringer(februar(2021), 3, 17_341, YTELSE) +
                posteringer(mai(2021), 7, 18_382, YTELSE) +
                // Teknisk postering
                posteringer(mai(2021), 4, -938, YTELSE) +
                // Reduser feilutbetaling til null
                posteringer(mai(2021), 4, -938, FEILUTBETALING) +
                // Motpost feilutbetaling
                posteringer(mai(2021), 4, 938, MOTP)

        val oppsummering =
            lagSimuleringsoppsummering(
                revurdering23nov.tilDetaljertSimuleringsresultat(),
                23.november(2021),
            )

        assertThat(oppsummering.feilutbetaling).isEqualTo(0.toBigDecimal())
        assertThat(oppsummering.etterbetaling).isEqualTo(1_686.toBigDecimal())
    }
}
