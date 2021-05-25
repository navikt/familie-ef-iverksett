package no.nav.familie.ef.iverksett.konsistensavstemming

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.domene.toMedMetadata
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.UtbetalingsoppdragGenerator
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingUtbetalingsoppdrag
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class KonsistensavstemmingService(
        private val oppdragKlient: OppdragClient,
        private val hentTilstand: HentTilstand
) {

    fun sendKonsistensavstemming(konsistensavstemmingDto: KonsistensavstemmingDto) {
        try {
            val behandlingIdTilKonsistensavstemmingTilkjentYtelseDtoMap = konsistensavstemmingDto.tilkjenteYtelser
                    .associateBy { it.behandlingId }

            val behandlingIdTilkjentYtelseForUtbetalingMap = hentTilstand
                    .hentTilkjentYtelseForUtbetalingListe(behandlingIdTilKonsistensavstemmingTilkjentYtelseDtoMap.keys)

            val utbetalingsoppdrag = behandlingIdTilkjentYtelseForUtbetalingMap
                    .map { (behandlingId,tilkjentYtelse)->

                        val konsistensavstemmingTilkjentYtelseDto = behandlingIdTilKonsistensavstemmingTilkjentYtelseDtoMap[behandlingId]!!

                        val tilkjentYtelseMedMetaData = tilkjentYtelse.toMedMetadata(
                             saksbehandlerId = "N/A",
                             eksternBehandlingId = konsistensavstemmingTilkjentYtelseDto.eksternId,
                             stønadType = konsistensavstemmingTilkjentYtelseDto.stønadType,
                             eksternFagsakId =konsistensavstemmingTilkjentYtelseDto.eksternFagsakId,
                             personIdent = konsistensavstemmingTilkjentYtelseDto.personIdent,
                             behandlingId = behandlingId,
                             vedtaksdato = konsistensavstemmingTilkjentYtelseDto.vedtaksdato
                        )

                        val tilkjentYtelseMedUtbetalingsoppdrag = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                                tilkjentYtelseMedMetaData,
                                tilkjentYtelse
                        )

                        tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                                                 ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

                    }


            val konsistensavstemmingUtbetalingsoppdrag = KonsistensavstemmingUtbetalingsoppdrag(
                    "EF",
                    utbetalingsoppdrag,
                    LocalDateTime.now()
            )


            oppdragKlient.konsistensavstemming(konsistensavstemmingUtbetalingsoppdrag)

        } catch (feil: Throwable) {
            throw Exception("Sending av utbetalingsoppdrag til konsistensavtemming feilet", feil)
        }
    }
}

interface HentTilstand {
    fun hentTilkjentYtelseForUtbetalingListe(behandlingIder: Collection<UUID>): Map<UUID, TilkjentYtelse>
}

@Repository
class HentTilstandJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : HentTilstand {

    override fun hentTilkjentYtelseForUtbetalingListe(behandlingIder: Collection<UUID>): Map<UUID, TilkjentYtelse> {
        TODO("Not yet implemented")
    }

}
