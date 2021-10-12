package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SimuleringService(
        private val oppdragKlient: OppdragClient,
        private val tilstandRepository: TilstandRepository
) {

    fun hentSimulering(simulering: Simulering): DetaljertSimuleringResultat {
        try {

            val forrigeTilkjentYtelse = simulering.forrigeBehandlingId?.let {
                tilstandRepository.hentTilkjentYtelse(simulering.forrigeBehandlingId)
            }

            val tilkjentYtelseMedUtbetalingsoppdrag = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simulering.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse
            )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                                     ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            return oppdragKlient.hentSimulering(utbetalingsoppdrag)
        } catch (feil: Throwable) {
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }

    fun hentBeriketSimulering(simulering: Simulering, tidSimuleringHentet: LocalDate): BeriketSimuleringsresultat {
        val detaljertSimuleringResultat = hentSimulering(simulering)
        val simuleringsresultatDto = lagSimuleringsoppsummering(detaljertSimuleringResultat, tidSimuleringHentet)

        return BeriketSimuleringsresultat(
                detaljer = detaljertSimuleringResultat,
                oppsummering = simuleringsresultatDto
        )
    }
}