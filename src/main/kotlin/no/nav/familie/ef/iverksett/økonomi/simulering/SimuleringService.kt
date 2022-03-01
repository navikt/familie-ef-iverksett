package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SimuleringService(
        private val oppdragKlient: OppdragClient,
        private val tilstandRepository: TilstandRepository,
        private val featureToggleService: FeatureToggleService
) {

    fun hentSimulering(simulering: Simulering): DetaljertSimuleringResultat {
        try {
            val forrigeTilkjentYtelse = simulering.forrigeBehandlingId?.let {
                tilstandRepository.hentTilkjentYtelse(simulering.forrigeBehandlingId)
            }

            val tilkjentYtelseMedUtbetalingsoppdrag = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simulering.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse,
                    featureToggleService.isEnabled("familie.ef.iverksett.opphoer-v2")
            )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                                     ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            if (utbetalingsoppdrag.utbetalingsperiode.isEmpty()) {
                return DetaljertSimuleringResultat(emptyList())
            }

            return oppdragKlient.hentSimulering(utbetalingsoppdrag)
        } catch (feil: Throwable) {
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }

    private fun erFørstegangsbehandlingUtenBeløp(simulering: Simulering) =
            simulering.forrigeBehandlingId == null && manglerEllerKunNullbeløp(simulering)

    private fun manglerEllerKunNullbeløp(simulering: Simulering): Boolean {
        val andeler = simulering.nyTilkjentYtelseMedMetaData.tilkjentYtelse.andelerTilkjentYtelse
        return andeler.isEmpty() || andeler.all { it.beløp == 0 }
    }

    fun hentBeriketSimulering(simulering: Simulering): BeriketSimuleringsresultat {
        val detaljertSimuleringResultat = hentSimulering(simulering)
        val simuleringsresultatDto = lagSimuleringsoppsummering(detaljertSimuleringResultat, LocalDate.now())

        return BeriketSimuleringsresultat(
                detaljer = detaljertSimuleringResultat,
                oppsummering = simuleringsresultatDto
        )
    }
}