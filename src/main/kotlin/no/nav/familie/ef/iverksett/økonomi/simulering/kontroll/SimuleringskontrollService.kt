package no.nav.familie.ef.iverksett.økonomi.simulering.kontroll

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.iverksetting.domene.tilSimulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.simulering.fagområdeKoderForPosteringer
import no.nav.familie.ef.iverksett.økonomi.simulering.lagSimuleringsoppsummering
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Service
class SimuleringskontrollService(
    private val simuleringskontrollRepository: SimuleringskontrollRepository,
    private val featureToggleService: FeatureToggleService,
    private val oppdragKlient: OppdragClient,
    private val iverksettResultatService: IverksettResultatService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun kontrollerMedNyUtbetalingsgenerator(
        iverksett: IverksettData,
        beriketSimuleringsresultatFn: () -> BeriketSimuleringsresultat,
    ) {
        if (!featureToggleService.isEnabled("familie.ef.iverksett.simuleringskontroll")) {
            return
        }
        if (iverksett.behandling.forrigeBehandlingId == null) {
            return
        }
        val beriketSimuleringsresultat = beriketSimuleringsresultatFn()
        val simulering = iverksett.tilSimulering()

        val behandlingId = iverksett.behandling.behandlingId
        try {
            simulerMedGammelUtbetalingsgeneratorOgSjekkDiff(simulering, beriketSimuleringsresultat)
        } catch (e: Exception) {
            logger.warn("Feilet kontroll av behandling=$behandlingId")
        }
    }

    private fun simulerMedGammelUtbetalingsgeneratorOgSjekkDiff(
        simulering: Simulering,
        beriketSimuleringsresultat: BeriketSimuleringsresultat,
    ) {
        val behandlingId = simulering.nyTilkjentYtelseMedMetaData.behandlingId
        val simuleringsresultatKontroll = hentKontrollsimulering(simulering)
        val iverksattePerioder = beriketSimuleringsresultat.oppsummering.perioder.sortedBy { it.fom }
        val kontrollPerioder = simuleringsresultatKontroll.oppsummering.perioder.sortedBy { it.fom }

        val harDiff = harDiff(behandlingId, iverksattePerioder, kontrollPerioder)
        if (harDiff) {
            val resultat = SimuleringskontrollResultat(simuleringsresultatKontroll)
            val input = SimuleringskontrollInput(simulering, beriketSimuleringsresultat)
            simuleringskontrollRepository.insert(Simuleringskontroll(behandlingId, input, resultat))
        }
        logger.info("behandling=$behandlingId - kontroll av ny utbetalingsgenerator utført - harDiff=$harDiff")
    }

    private fun hentKontrollsimulering(simulering: Simulering): BeriketSimuleringsresultat {
        val detaljertSimuleringResultat = hentKontrollResultat(simulering)
        val simuleringsresultatDto = lagSimuleringsoppsummering(detaljertSimuleringResultat, LocalDate.now())

        return BeriketSimuleringsresultat(
            detaljer = detaljertSimuleringResultat,
            oppsummering = simuleringsresultatDto,
        )
    }

    private fun hentKontrollResultat(
        simulering: Simulering,
    ): DetaljertSimuleringResultat {
        if (featureToggleService.isEnabled("familie.ef.iverksett.stopp-iverksetting")) {
            error("Iverksetting er skrudd av - kan ikke simulere nå")
        }
        try {
            val forrigeTilkjentYtelse = simulering.forrigeBehandlingId?.let {
                iverksettResultatService.hentTilkjentYtelse(simulering.forrigeBehandlingId)
            }

            logger.info("Kontrollsimulerer for behandling=${simulering.nyTilkjentYtelseMedMetaData.behandlingId}")
            val tilkjentYtelseMedUtbetalingsoppdrag =
                UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simulering.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse,
                )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            if (utbetalingsoppdrag.utbetalingsperiode.isEmpty()) {
                return DetaljertSimuleringResultat(emptyList())
            }
            return hentSimuleringsresultatOgFiltrerPosteringer(
                utbetalingsoppdrag,
                simulering.nyTilkjentYtelseMedMetaData.stønadstype,
            )
        } catch (feil: Throwable) {
            val cause = feil.cause
            if (feil is RessursException && cause is HttpClientErrorException.BadRequest) {
                throw ApiFeil(feil.ressurs.melding, HttpStatus.BAD_REQUEST)
            }
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }

    private fun hentSimuleringsresultatOgFiltrerPosteringer(
        utbetalingsoppdrag: Utbetalingsoppdrag,
        stønadType: StønadType,
    ): DetaljertSimuleringResultat {
        val fagOmrådeKoder = fagområdeKoderForPosteringer(stønadType)
        val simuleringsResultat = oppdragKlient.hentSimuleringsresultat(utbetalingsoppdrag)
        return simuleringsResultat.copy(
            simuleringsResultat.simuleringMottaker
                .map { mottaker ->
                    mottaker.copy(
                        simulertPostering = mottaker.simulertPostering.filter { postering ->
                            fagOmrådeKoder.contains(postering.fagOmrådeKode)
                        },
                    )
                },
        )
    }

    private fun harDiff(
            behandlingId: UUID,
            iverksattePerioder: List<Simuleringsperiode>,
            kontrollPerioder: List<Simuleringsperiode>,
    ): Boolean {
        var harDiff = false

        val førstePeriodeIverksatt = iverksattePerioder.firstOrNull()
        val førstePeriodeKontroll = kontrollPerioder.firstOrNull()
        if (førstePeriodeIverksatt == null || førstePeriodeKontroll == null) {
            logger.info(
                    "behandlingId=$behandlingId - kjørerIkkeKontroll" +
                    " førstePeriodeIverksattErNull=${førstePeriodeIverksatt == null}" +
                    " førstePeriodeKontrollErNull=${førstePeriodeKontroll == null}",
            )
            return false
        }

        iverksattePerioder
            .filter { it.tom < førstePeriodeKontroll.fom }
            .filter { it.resultat.harDiff() }
            .takeIf { it.isNotEmpty() }
            ?.run {
                logger.warn("behandlingId=$behandlingId - Har diff i resultat før ny simuleringsendring")
                harDiff = true
            }

        val iverksattePerioderMap = extracted(iverksattePerioder)
        val kontrollPerioderMap = extracted(kontrollPerioder)

        kontrollPerioderMap.forEach { (måned, resultatMedKontrollUG) ->
            val resultatMedIverksattUG = iverksattePerioderMap[måned]
            if (resultatMedIverksattUG != null && resultatMedIverksattUG != resultatMedKontrollUG) {
                logger.warn("behandlingId=$behandlingId - måned=$måned resultatMedIverksattUG=$resultatMedIverksattUG resultatMedKontrollUG=$resultatMedKontrollUG")
                harDiff = true
            }
        }
        if (kontrollPerioderMap.size != iverksattePerioderMap.size) {
            if (iverksattePerioderMap.size > kontrollPerioderMap.size) {
                logger.warn("behandlingId=$behandlingId - diff i antall måneder. Nå: ${kontrollPerioder.size} Tidligere: ${iverksattePerioder.size}. Er nullbeløp i første periode? ${førstePeriodeIverksatt.nyttBeløp == BigDecimal.ZERO}")
            } else {
                logger.info("behandlingId=$behandlingId - diff i antall måneder. Nå: ${kontrollPerioder.size} Tidligere: ${iverksattePerioder.size}")
            }
            harDiff = true
        }
        return harDiff
    }

    private fun extracted(tidligerePerioder: List<Simuleringsperiode>): Map<YearMonth, Int> {
        val map = mutableMapOf<YearMonth, Int>()
        tidligerePerioder.forEach { periode ->
            periode.split().forEach {
                val previous = map.put(it.first, it.second)
                if (previous != null) {
                    logger.error("Har flere verdier for ${it.first}")
                }
            }
        }
        return map
    }
}

private fun Simuleringsperiode.split(): List<Pair<YearMonth, Int>> {
    val perioder = mutableListOf<Pair<YearMonth, Int>>()
    var måned = YearMonth.from(this.fom)
    while (måned <= YearMonth.from(this.tom)) {
        perioder.add(Pair(måned, this.resultat.toInt()))
        måned = måned.plusMonths(1)
    }
    return perioder
}

private fun BigDecimal.harDiff() = this.compareTo(BigDecimal.ZERO) != 0
