package no.nav.familie.ef.iverksett.økonomi.konsistens

import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsgeneratorHelper
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.lagPeriodeFraAndel
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingDto
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingTilkjentYtelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class KonsistensavstemmingService(
    private val oppdragKlient: OppdragClient,
    private val iverksettResultatService: IverksettResultatService,
) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendKonsistensavstemming(
        konsistensavstemmingDto: KonsistensavstemmingDto,
        sendStartmelding: Boolean = true,
        sendAvsluttmelding: Boolean = true,
        transaksjonId: UUID? = null,
    ) {
        try {
            val utbetalingsoppdrag = lagUtbetalingsoppdragForKonsistensavstemming(konsistensavstemmingDto)
            val konsistensavstemmingUtbetalingsoppdrag = UtbetalingsgeneratorHelper.KonsistensavstemmingUtbetalingsoppdrag(
                fagsystem = konsistensavstemmingDto.stønadType.tilKlassifisering(),
                utbetalingsoppdrag = utbetalingsoppdrag,
                avstemmingstidspunkt = konsistensavstemmingDto.avstemmingstidspunkt ?: LocalDateTime.now(),
            )
            oppdragKlient.konsistensavstemming(
                konsistensavstemmingUtbetalingsoppdrag,
                sendStartmelding,
                sendAvsluttmelding,
                transaksjonId,
            )
        } catch (feil: Throwable) {
            throw Exception("Sending av utbetalingsoppdrag til konsistensavtemming feilet", feil)
        }
    }

    private fun lagUtbetalingsoppdragForKonsistensavstemming(konsistensavstemmingDto: KonsistensavstemmingDto): List<Utbetalingsoppdrag> {
        if (konsistensavstemmingDto.tilkjenteYtelser.isEmpty()) return emptyList()

        val stønadType = konsistensavstemmingDto.stønadType
        val tilkjentYtelsePerBehandlingId = konsistensavstemmingDto.tilkjenteYtelser.associateBy { it.behandlingId }
        val behandlingIdTilkjentYtelseForUtbetalingMap =
            iverksettResultatService.hentTilkjentYtelse(tilkjentYtelsePerBehandlingId.keys)

        return behandlingIdTilkjentYtelseForUtbetalingMap.map { (behandlingId, tilkjentYtelse) ->
            genererUtbetalingsoppdrag(
                tilkjentYtelsePerBehandlingId[behandlingId]!!,
                tilkjentYtelse,
                behandlingId,
                stønadType,
            )
        }
    }

    private fun genererUtbetalingsoppdrag(
        konsistensavstemmingTilkjentYtelseDto: KonsistensavstemmingTilkjentYtelseDto,
        tilkjentYtelse: TilkjentYtelse,
        behandlingId: UUID,
        stønadType: StønadType,
    ): Utbetalingsoppdrag {
        val personIdent = konsistensavstemmingTilkjentYtelseDto.personIdent
        val eksternBehandlingId = konsistensavstemmingTilkjentYtelseDto.eksternBehandlingId

        if (tilkjentYtelse.utbetalingsoppdrag == null) {
            error("Savner utbetalingsoppdrag i tilkjent ytelse fra tilstand for behandling=$behandlingId")
        }

        val andelerFraRequest = konsistensavstemmingTilkjentYtelseDto.andelerTilkjentYtelse.map { it.toDomain() }

        val andeler = tilkjentYtelse.andelerTilkjentYtelse.filter {
            andelerFraRequest.any { andel -> beløpOgPeriodeErLik(andel, it) }
        }

        if (andelerFraRequest.size != andeler.size) {
            secureLogger.info(
                "Forskjell i andeler for behandling=$behandlingId" +
                    " request=$andelerFraRequest" +
                    " iverksettAndeler=${tilkjentYtelse.andelerTilkjentYtelse}",
            )
            error("Finner ikke riktige periodebeløp i det som er lagret for behandling=$behandlingId")
        }

        return Utbetalingsoppdrag(
            kodeEndring = Utbetalingsoppdrag.KodeEndring.NY, // er ikke i bruk ved konsistensavstemming
            fagSystem = stønadType.tilKlassifisering(),
            saksnummer = konsistensavstemmingTilkjentYtelseDto.eksternFagsakId.toString(),
            aktoer = personIdent,
            saksbehandlerId = tilkjentYtelse.utbetalingsoppdrag.saksbehandlerId,
            avstemmingTidspunkt = LocalDateTime.now(),
            utbetalingsperiode = andeler.map {
                lagPeriodeFraAndel(
                    andel = it,
                    type = stønadType,
                    eksternBehandlingId = eksternBehandlingId,
                    vedtaksdato = LocalDate.now(), // er ikke i bruk ved konsistensavstemming
                    personIdent = personIdent,
                )
            },
        )
    }

    /**
     * Når vi skal finne finne frem andeler fra databasen som vi har sendt ivei er det tilsrekkelig å kun matche beløp og periode
     */
    private fun beløpOgPeriodeErLik(a: AndelTilkjentYtelse, b: AndelTilkjentYtelse) =
        a.beløp == b.beløp &&
            a.periode == b.periode
}
