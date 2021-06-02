package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.PeriodeId
import no.nav.familie.ef.iverksett.økonomi.nullAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.ef.iverksett.økonomi.tilPeriodeId
import no.nav.familie.ef.iverksett.økonomi.ØkonomiUtils.andelTilOpphørMedDato
import no.nav.familie.ef.iverksett.økonomi.ØkonomiUtils.andelerTilOpprettelse
import no.nav.familie.ef.iverksett.økonomi.ØkonomiUtils.beståendeAndeler
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.ENDR
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.NY
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.time.LocalDate
import java.util.*

object UtbetalingsoppdragGenerator {

    /**
     * Lager utbetalingsoppdrag med kjedede perioder av andeler.
     * Ved opphør sendes @param[nyTilkjentYtelseMedMetaData] uten andeler.
     *
     * @param[nyTilkjentYtelseMedMetaData] Den nye tilkjente ytelsen, med fullstending sett av andeler
     * @param[forrigeTilkjentYtelse] Forrige tilkjent ytelse, med fullstendig sett av andeler med id
     * @return Ny tilkjent ytelse med andeler med id'er, samt utbetalingsoppdrag
     */
    fun lagTilkjentYtelseMedUtbetalingsoppdrag(nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
                                               forrigeTilkjentYtelse: TilkjentYtelse? = null): TilkjentYtelse {
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        val andelerNyTilkjentYtelse = andelerUtenNullVerdier(nyTilkjentYtelse)
        val andelerForrigeTilkjentYtelse = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val sistePeriodeIdIForrigeKjede = sistePeriodeId(forrigeTilkjentYtelse)

        val beståendeAndeler = beståendeAndeler(andelerForrigeTilkjentYtelse, andelerNyTilkjentYtelse)
        val andelTilOpphørMedDato = andelTilOpphørMedDato(andelerForrigeTilkjentYtelse, andelerNyTilkjentYtelse)
        val andelerTilOpprettelse = andelerTilOpprettelse(andelerNyTilkjentYtelse, beståendeAndeler)

        val andelerTilOpprettelseMedPeriodeId =
                lagAndelerMedPeriodeId(andelerTilOpprettelse, sistePeriodeIdIForrigeKjede, nyTilkjentYtelseMedMetaData.behandlingId)

        val utbetalingsperioderSomOpprettes =
                lagUtbetalingsperioderForOpprettelse(andeler = andelerTilOpprettelseMedPeriodeId,
                                                     behandlingId = nyTilkjentYtelseMedMetaData.eksternBehandlingId,
                                                     tilkjentYtelse = nyTilkjentYtelseMedMetaData,
                                                     type = nyTilkjentYtelseMedMetaData.stønadstype)

        val utbetalingsperioderSomOpphøres = andelTilOpphørMedDato?.let {
            lagUtbetalingsperioderForOpphør(andeler = andelTilOpphørMedDato,
                                            tilkjentYtelse = nyTilkjentYtelseMedMetaData,
                                            behandlingId = nyTilkjentYtelseMedMetaData.eksternBehandlingId,
                                            type = nyTilkjentYtelseMedMetaData.stønadstype)
        } ?: emptyList()

        val utbetalingsoppdrag =
                Utbetalingsoppdrag(saksbehandlerId = nyTilkjentYtelseMedMetaData.saksbehandlerId,
                                   kodeEndring = if (forrigeTilkjentYtelse == null) NY else ENDR,
                                   fagSystem = nyTilkjentYtelseMedMetaData.stønadstype.tilKlassifisering(),
                                   saksnummer = nyTilkjentYtelseMedMetaData.eksternFagsakId.toString(),
                                   aktoer = nyTilkjentYtelseMedMetaData.personIdent,
                                   utbetalingsperiode = listOf(utbetalingsperioderSomOpprettes,
                                                               utbetalingsperioderSomOpphøres)
                                           .flatten()
                                           .sortedBy { it.periodeId }
                )

        val gjeldendeAndeler = (beståendeAndeler + andelerTilOpprettelseMedPeriodeId)
                .ellerNullAndel(nyTilkjentYtelseMedMetaData, sistePeriodeIdIForrigeKjede)

        return nyTilkjentYtelse.copy(utbetalingsoppdrag = utbetalingsoppdrag,
                                     andelerTilkjentYtelse = gjeldendeAndeler)
        //TODO legge til startperiode, sluttperiode, opphørsdato. Se i BA-sak - legges på i konsistensavstemming?
    }

    /**
     * Hvis det ikke er noen andeler igjen, må vi opprette en "null-andel" som tar vare på periodeId'en for ytelsestypen
     */
    private fun List<AndelTilkjentYtelse>.ellerNullAndel(nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
                                                         sistePeriodeIdIForrigeKjede: PeriodeId?): List<AndelTilkjentYtelse> {
        return if (this.isEmpty()) {
            listOf(nullAndelTilkjentYtelse(nyTilkjentYtelseMedMetaData.behandlingId,
                                           sistePeriodeIdIForrigeKjede))

        } else {
            this
        }
    }

    private fun lagUtbetalingsperioderForOpphør(andeler: Pair<AndelTilkjentYtelse, LocalDate>,
                                                behandlingId: Long,
                                                type: StønadType,
                                                tilkjentYtelse: TilkjentYtelseMedMetaData): List<Utbetalingsperiode> {
        val utbetalingsperiodeMal = UtbetalingsperiodeMal(tilkjentYtelse, true)
        val (sisteAndelIKjede, opphørKjedeFom) = andeler
        return listOf(utbetalingsperiodeMal.lagPeriodeFraAndel(andel = sisteAndelIKjede,
                                                               behandlingId = behandlingId,
                                                               type = type,
                                                               opphørKjedeFom = opphørKjedeFom))
    }

    private fun lagUtbetalingsperioderForOpprettelse(andeler: List<AndelTilkjentYtelse>,
                                                     behandlingId: Long,
                                                     type: StønadType,
                                                     tilkjentYtelse: TilkjentYtelseMedMetaData): List<Utbetalingsperiode> {

        val utbetalingsperiodeMal = UtbetalingsperiodeMal(tilkjentYtelse)
        return andeler.map { utbetalingsperiodeMal.lagPeriodeFraAndel(it, type, behandlingId) }
    }

    private fun lagAndelerMedPeriodeId(andeler: List<AndelTilkjentYtelse>,
                                       sisteOffsetIKjedeOversikt: PeriodeId?,
                                       behandlingId: UUID): List<AndelTilkjentYtelse> {
        val forrigePeriodeIdIKjede: Long? = sisteOffsetIKjedeOversikt?.gjeldende
        val nestePeriodeIdIKjede = forrigePeriodeIdIKjede?.plus(1) ?: 1

        return andeler.sortedBy { it.periodebeløp.fraOgMed }.mapIndexed { index, andel ->
            andel.copy(periodeId = nestePeriodeIdIKjede + index,
                       kildeBehandlingId = behandlingId,
                       forrigePeriodeId = if (index == 0) forrigePeriodeIdIKjede else nestePeriodeIdIKjede + index - 1)
        }
    }

    private fun sistePeriodeId(tilkjentYtelse: TilkjentYtelse?): PeriodeId? {
        return tilkjentYtelse?.let { ytelse ->
            ytelse.andelerTilkjentYtelse.filter { it.periodeId != null }.maxByOrNull { it.periodeId!! }?.tilPeriodeId()
        }
    }

    private fun andelerUtenNullVerdier(tilkjentYtelse: TilkjentYtelse?): List<AndelTilkjentYtelse> =
            tilkjentYtelse?.andelerTilkjentYtelse?.filter { !it.erNull() } ?: emptyList()
}