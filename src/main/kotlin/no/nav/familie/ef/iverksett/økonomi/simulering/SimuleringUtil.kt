package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.PosteringType.FEILUTBETALING
import no.nav.familie.kontrakter.felles.simulering.PosteringType.YTELSE
import no.nav.familie.kontrakter.felles.simulering.SimuleringMottaker
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import no.nav.familie.kontrakter.felles.simulering.SimulertPostering
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.LocalDate

fun lagSimuleringsoppsummering(detaljertSimuleringResultat: DetaljertSimuleringResultat,
                               tidSimuleringHentet: LocalDate): Simuleringsoppsummering {
    val perioder = grupperPosteringerEtterDato(detaljertSimuleringResultat.simuleringMottaker)

    val framtidigePerioder =
            perioder.filter {
                it.fom > tidSimuleringHentet ||
                (it.tom > tidSimuleringHentet && it.forfallsdato > tidSimuleringHentet)
            }

    val nestePeriode = framtidigePerioder.filter { it.feilutbetaling == ZERO }.minByOrNull { it.fom }
    val tomSisteUtbetaling = perioder.filter { nestePeriode == null || it.fom < nestePeriode.fom }.maxOfOrNull { it.tom }

    return Simuleringsoppsummering(
            perioder = perioder,
            fomDatoNestePeriode = nestePeriode?.fom,
            etterbetaling = hentTotalEtterbetaling(perioder, nestePeriode?.fom),
            feilutbetaling = hentTotalFeilutbetaling(perioder, nestePeriode?.fom).let { maxOf(it, ZERO) },
            fom = perioder.minOfOrNull { it.fom },
            tomDatoNestePeriode = nestePeriode?.tom,
            forfallsdatoNestePeriode = nestePeriode?.forfallsdato,
            tidSimuleringHentet = tidSimuleringHentet,
            tomSisteUtbetaling = tomSisteUtbetaling,
    )
}

fun grupperPosteringerEtterDato(mottakere: List<SimuleringMottaker>): List<Simuleringsperiode> {
    return mottakere
            .flatMap { it.simulertPostering }
            .filter { it.posteringType == FEILUTBETALING || it.posteringType == YTELSE }
            .groupBy { PeriodeMedForfall(fom = it.fom, tom = it.tom, forfallsdato = it.forfallsdato) }
            .map { (periodeMedForfall, posteringListe) ->
                Simuleringsperiode(
                        periodeMedForfall.fom,
                        periodeMedForfall.tom,
                        periodeMedForfall.forfallsdato,
                        nyttBeløp = hentNyttBeløp(posteringListe),
                        tidligereUtbetalt = hentTidligereUtbetalt(posteringListe),
                        resultat = hentResultat(posteringListe),
                        feilutbetaling = hentFeilutbetaling(posteringListe),
                        etterbetaling = hentEtterbetaling(posteringListe)
                )
            }
}

private fun hentNyttBeløp(posteringer: List<SimulertPostering>): BigDecimal {
    val sumPositiveYtelser = posteringer
            .filter { it.posteringType == YTELSE && it.beløp > ZERO }
            .sumOf { it.beløp }

    val feilutbetaling =  hentFeilutbetaling(posteringer)

    return if (feilutbetaling > ZERO) sumPositiveYtelser - feilutbetaling else sumPositiveYtelser
}

private fun hentFeilutbetaling(posteringer: List<SimulertPostering>) =
        posteringer
                .filter { it.posteringType == FEILUTBETALING }
                .sumOf { it.beløp }

private fun hentTidligereUtbetalt(posteringer: List<SimulertPostering>): BigDecimal {
    val sumNegativeYtelser = posteringer
            .filter { it.posteringType === YTELSE && it.beløp < ZERO }
            .sumOf { it.beløp }

    val feilutbetaling = hentFeilutbetaling(posteringer)
    return if (feilutbetaling < ZERO) -(sumNegativeYtelser - feilutbetaling) else -sumNegativeYtelser
}

private fun hentResultat(posteringer: List<SimulertPostering>): BigDecimal {
    val posteringerHarFeilutbetaling = posteringer.any { it.posteringType == FEILUTBETALING }
    return when {
        posteringerHarFeilutbetaling -> posteringer
                .filter { it.posteringType == FEILUTBETALING }
                .sumOf { -it.beløp }
        else -> posteringer.sumOf { it.beløp }
    }
}

private fun hentEtterbetaling(posteringer: List<SimulertPostering>): BigDecimal {
    val posteringerHarPositivFeilutbetaling =
            posteringer.any { it.posteringType == FEILUTBETALING && it.beløp > ZERO }

    val sumYtelser = posteringer
            .filter { it.posteringType == YTELSE }
            .sumOf { it.beløp }

    return when {
        posteringerHarPositivFeilutbetaling -> ZERO
        else -> maxOf(sumYtelser, ZERO)
    }
}

private fun hentTotalEtterbetaling(simuleringsperioder: List<Simuleringsperiode>, fomDatoNestePeriode: LocalDate?) =
        simuleringsperioder
                .filter { (fomDatoNestePeriode == null || it.fom < fomDatoNestePeriode) }
                .sumOf { it.etterbetaling ?: ZERO }
                .let { maxOf(it, ZERO) }

private fun hentTotalFeilutbetaling(simuleringsperioder: List<Simuleringsperiode>, fomDatoNestePeriode: LocalDate?) =
        simuleringsperioder
                .filter { fomDatoNestePeriode == null || it.fom < fomDatoNestePeriode }
                .sumOf { it.feilutbetaling }

private data class PeriodeMedForfall(
        val fom: LocalDate,
        val tom: LocalDate,
        val forfallsdato: LocalDate
)

fun BeriketSimuleringsresultat.harFeilutbetaling(): Boolean {
    return this.oppsummering.feilutbetaling > ZERO
}