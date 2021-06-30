package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.springframework.stereotype.Service

@Service
class SimuleringService(
        private val oppdragKlient: OppdragClient,
        private val tilstandRepository: TilstandRepository
) {

    fun hentSimulering(simuleringDto: SimuleringDto): DetaljertSimuleringResultat {
        try {

            val forrigeTilkjentYtelse = tilstandRepository.hentTilkjentYtelse(simuleringDto.forrigeBehandlingId)

            val tilkjentYtelseMedUtbetalingsoppdrag = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simuleringDto.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse
            )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                                     ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            return oppdragKlient.hentSimulering(utbetalingsoppdrag)
        } catch (feil: Throwable) {
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }
}