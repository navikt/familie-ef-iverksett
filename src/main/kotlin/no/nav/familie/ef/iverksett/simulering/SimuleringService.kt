package no.nav.familie.ef.iverksett.simulering

import no.nav.familie.ef.iverksett.common.assertGenerelleSuksessKriterier
import no.nav.familie.ef.iverksett.økonomi.UtbetalingsoppdragGenerator
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.springframework.stereotype.Service

@Service
class SimuleringService(
        private val simuleringKlient: SimuleringKlient
) {

    fun hentSimulering(simuleringDto: SimuleringDto): DetaljertSimuleringResultat? {
        try {
            val tilkjentYtelseMedUtbetalingsoppdrag = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simuleringDto.nyTilkjentYtelseMedMetaData,
                    simuleringDto.forrigeTilkjentYtelse
            )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag

            if (utbetalingsoppdrag == null || utbetalingsoppdrag.utbetalingsperiode.isEmpty()) {
                return null
            }

            val simuleringResponse = simuleringKlient.hentSimulering(utbetalingsoppdrag)

            assertGenerelleSuksessKriterier(simuleringResponse.body)
            return simuleringResponse.body?.data!!
        } catch (feil: Throwable) {
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }
}