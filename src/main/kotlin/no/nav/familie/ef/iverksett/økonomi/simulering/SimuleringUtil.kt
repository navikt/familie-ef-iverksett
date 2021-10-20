package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.PosteringType
import no.nav.familie.kontrakter.felles.simulering.SimuleringMottaker
import no.nav.familie.kontrakter.felles.simulering.SimulertPostering
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import java.math.BigDecimal
import java.time.LocalDate

fun lagSimuleringsoppsummering(detaljertSimuleringResultat: DetaljertSimuleringResultat, tidSimuleringHentet: LocalDate): Simuleringsoppsummering {
    val perioder = grupperPosteringerEtterDato(detaljertSimuleringResultat.simuleringMottaker)

    val framtidigePerioder =
            perioder.filter {
                it.fom > tidSimuleringHentet ||
                (it.tom > tidSimuleringHentet && it.forfallsdato > tidSimuleringHentet)
            }

    val nestePeriode = framtidigePerioder.filter { it.feilutbetaling == BigDecimal.ZERO }.minByOrNull { it.fom }
    val tomSisteUtbetaling = perioder.filter { nestePeriode == null || it.fom < nestePeriode.fom }.maxOfOrNull { it.tom }

    return Simuleringsoppsummering(
            perioder = perioder,
            fomDatoNestePeriode = nestePeriode?.fom,
            etterbetaling = hentTotalEtterbetaling(perioder, nestePeriode?.fom),
            feilutbetaling = hentTotalFeilutbetaling(perioder, nestePeriode?.fom),
            fom = perioder.minOfOrNull { it.fom },
            tomDatoNestePeriode = nestePeriode?.tom,
            forfallsdatoNestePeriode = nestePeriode?.forfallsdato,
            tidSimuleringHentet = tidSimuleringHentet,
            tomSisteUtbetaling = tomSisteUtbetaling,
    )
}

private fun grupperPosteringerEtterDato(mottakere: List<SimuleringMottaker>): List<Simuleringsperiode> {
    val simuleringsperioder = mutableMapOf<LocalDate, MutableList<SimulertPostering>>()

    mottakere.forEach {
        it.simulertPostering.filter { it.posteringType == PosteringType.YTELSE || it.posteringType == PosteringType.FEILUTBETALING }
                .forEach { postering ->
                    if (simuleringsperioder.containsKey(postering.fom))
                        simuleringsperioder[postering.fom]?.add(postering)
                    else simuleringsperioder[postering.fom] = mutableListOf(postering)
                }
    }

    return simuleringsperioder.map { (fom, posteringListe) ->
        Simuleringsperiode(
                fom,
                posteringListe[0].tom,
                posteringListe[0].forfallsdato,
                nyttBeløp = hentNyttBeløp(posteringListe),
                tidligereUtbetalt = hentTidligereUtbetalt(posteringListe),
                resultat = hentResultat(posteringListe),
                feilutbetaling = hentFeilutbetaling(posteringListe),
        )
    }
}

private fun hentNyttBeløp(posteringer: List<SimulertPostering>): BigDecimal {
    val sumPositiveYtelser = posteringer.filter { postering ->
        postering.posteringType == PosteringType.YTELSE && postering.beløp > BigDecimal.ZERO
    }.sumOf { it.beløp }
    val feilutbetaling = hentFeilutbetaling(posteringer)
    return if (feilutbetaling > BigDecimal.ZERO) sumPositiveYtelser - feilutbetaling else sumPositiveYtelser
}

private fun hentFeilutbetaling(posteringer: List<SimulertPostering>) =
        posteringer.filter { postering ->
            postering.posteringType == PosteringType.FEILUTBETALING
        }.sumOf { it.beløp }

private fun hentTidligereUtbetalt(posteringer: List<SimulertPostering>): BigDecimal {
    val sumNegativeYtelser = posteringer.filter { postering ->
        (postering.posteringType === PosteringType.YTELSE && postering.beløp < BigDecimal.ZERO)
    }.sumOf { -it.beløp }
    val feilutbetaling = hentFeilutbetaling(posteringer)
    return if (feilutbetaling < BigDecimal.ZERO) sumNegativeYtelser - feilutbetaling else sumNegativeYtelser
}

private fun hentResultat(posteringer: List<SimulertPostering>) =
        if (posteringer.map { it.posteringType }.contains(PosteringType.FEILUTBETALING)) {
            posteringer.filter {
                it.posteringType == PosteringType.FEILUTBETALING
            }.sumOf { -it.beløp }
        } else
            posteringer.sumOf { it.beløp }

private fun hentTotalEtterbetaling(simuleringPerioder: List<Simuleringsperiode>, fomDatoNestePeriode: LocalDate?) =
        simuleringPerioder.filter {
            it.resultat > BigDecimal.ZERO && (fomDatoNestePeriode == null || it.fom < fomDatoNestePeriode)
        }.sumOf { it.resultat }


private fun hentTotalFeilutbetaling(simuleringPerioder: List<Simuleringsperiode>, fomDatoNestePeriode: LocalDate?) =
        simuleringPerioder.filter { fomDatoNestePeriode == null || it.fom < fomDatoNestePeriode }.sumOf { it.feilutbetaling }


