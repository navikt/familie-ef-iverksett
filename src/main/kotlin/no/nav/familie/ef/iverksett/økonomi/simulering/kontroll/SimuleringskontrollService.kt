package no.nav.familie.ef.iverksett.økonomi.simulering.kontroll

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.iverksetting.domene.tilSimulering
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID

@Service
class SimuleringskontrollService(
    private val simuleringService: SimuleringService,
    private val simuleringskontrollRepository: SimuleringskontrollRepository,
    private val featureToggleService: FeatureToggleService,
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
            simulerMedNyUtbetalingsgeneratorOgSjekkDif(simulering, beriketSimuleringsresultat)
        } catch (e: Exception) {
            logger.warn("Feilet kontroll av behandling=$behandlingId")
        }
    }

    private fun simulerMedNyUtbetalingsgeneratorOgSjekkDif(
        simulering: Simulering,
        beriketSimuleringsresultat: BeriketSimuleringsresultat,
    ) {
        val behandlingId = simulering.nyTilkjentYtelseMedMetaData.behandlingId
        val simuleringNyUtbetalingsgenerator =
            simuleringService.hentBeriketSimulering(simulering, brukNyUtbetalingsgenerator = true)
        val tidligerePerioder = beriketSimuleringsresultat.oppsummering.perioder.sortedBy { it.fom }
        val nyePerioder = simuleringNyUtbetalingsgenerator.oppsummering.perioder.sortedBy { it.fom }

        val harDiff = harDiff(behandlingId, tidligerePerioder, nyePerioder)
        if (harDiff) {
            val resultat = SimuleringskontrollResultat(simuleringNyUtbetalingsgenerator)
            val input = SimuleringskontrollInput(simulering, beriketSimuleringsresultat)
            simuleringskontrollRepository.insert(Simuleringskontroll(behandlingId, input, resultat))
        }
        logger.info("behandling=$behandlingId - kontroll av ny utbetalingsgenerator utført - harDiff=$harDiff")
    }

    private fun harDiff(
        behandlingId: UUID,
        perioderMedGammelUtbetalingsgenerator: List<Simuleringsperiode>,
        perioderMedNyUtbetalingsgenerator: List<Simuleringsperiode>,
    ): Boolean {
        var harDiff = false

        val førstePeriodeMedGammelUG = perioderMedGammelUtbetalingsgenerator.firstOrNull()
        val førstePeriodeMedNyUG = perioderMedNyUtbetalingsgenerator.firstOrNull()
        if (førstePeriodeMedGammelUG == null || førstePeriodeMedNyUG == null) {
            logger.info(
                "behandlingId=$behandlingId - kjørerIkkeKontroll" +
                    " tidligereFørstePeriodeErNull=${førstePeriodeMedGammelUG == null}" +
                    " nyFørstePeriodeErNull=${førstePeriodeMedNyUG == null}",
            )
            return false
        }

        perioderMedGammelUtbetalingsgenerator
            .filter { it.tom < førstePeriodeMedNyUG.fom }
            .filter { it.resultat.harDiff() }
            .takeIf { it.isNotEmpty() }
            ?.run {
                logger.warn("behandlingId=$behandlingId - Har diff i resultat før ny simuleringsendring")
                harDiff = true
            }

        val perioderMedGammelUtbetalingsgeneratorMap =
            extracted(perioderMedGammelUtbetalingsgenerator).filterKeys { it >= YearMonth.from(førstePeriodeMedNyUG.fom) }
        val perioderMedNyUtbetalingsgeneratorMap = extracted(perioderMedNyUtbetalingsgenerator)

        perioderMedNyUtbetalingsgeneratorMap.forEach { (måned, resultatMedNyUG) ->
            val resultatMedGammelUG = perioderMedGammelUtbetalingsgeneratorMap[måned]
            if (resultatMedGammelUG != null && resultatMedGammelUG != resultatMedNyUG) {
                logger.warn("behandlingId=$behandlingId - måned=$måned resultatMedGammelUG=$resultatMedGammelUG resultatMedNyUG=$resultatMedNyUG")
                harDiff = true
            }
        }
        if (perioderMedNyUtbetalingsgeneratorMap.size != perioderMedGammelUtbetalingsgeneratorMap.size) {
            logger.warn("behandlingId=$behandlingId - diff i antall måneder")
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
