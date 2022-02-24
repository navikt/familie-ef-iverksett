package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerTilOpprettelse
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerUtenNullVerdier
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.beståendeAndeler
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.utbetalingsperiodeForOpphør
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.validerOpphørsdato
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.ENDR
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.NY
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.util.UUID

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
        validerOpphørsdato(nyTilkjentYtelseMedMetaData, forrigeTilkjentYtelse)
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        val andelerNyTilkjentYtelse = andelerUtenNullVerdier(nyTilkjentYtelse)
        val andelerForrigeTilkjentYtelse = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val sistePeriodeIdIForrigeKjede = sistePeriodeId(forrigeTilkjentYtelse)

        val beståendeAndeler = beståendeAndeler(andelerForrigeTilkjentYtelse, andelerNyTilkjentYtelse)
        val andelerTilOpprettelse = andelerTilOpprettelse(andelerNyTilkjentYtelse, beståendeAndeler)

        val andelerTilOpprettelseMedPeriodeId = lagAndelerMedPeriodeId(andelerTilOpprettelse,
                                                                       sistePeriodeIdIForrigeKjede,
                                                                       nyTilkjentYtelseMedMetaData.behandlingId)

        val utbetalingsperioderSomOpprettes = lagUtbetalingsperioderForOpprettelse(andeler = andelerTilOpprettelseMedPeriodeId,
                                                                                   tilkjentYtelse = nyTilkjentYtelseMedMetaData)

        val utbetalingsperiodeSomOpphøres = utbetalingsperiodeForOpphør(forrigeTilkjentYtelse, nyTilkjentYtelseMedMetaData)

        val utbetalingsperioder = (utbetalingsperioderSomOpprettes + utbetalingsperiodeSomOpphøres)
                .filterNotNull()
                .sortedBy { it.periodeId }
        val utbetalingsoppdrag =
                Utbetalingsoppdrag(saksbehandlerId = nyTilkjentYtelseMedMetaData.saksbehandlerId,
                                   kodeEndring = if (erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse)) NY else ENDR,
                                   fagSystem = nyTilkjentYtelseMedMetaData.stønadstype.tilKlassifisering(),
                                   saksnummer = nyTilkjentYtelseMedMetaData.eksternFagsakId.toString(),
                                   aktoer = nyTilkjentYtelseMedMetaData.personIdent,
                                   utbetalingsperiode = utbetalingsperioder)

        val gjeldendeAndeler = (beståendeAndeler + andelerTilOpprettelseMedPeriodeId)
                .ellerNullAndel(nyTilkjentYtelseMedMetaData, sistePeriodeIdIForrigeKjede)

        val sisteAndelIKjede = sisteAndelIKjede(gjeldendeAndeler, forrigeTilkjentYtelse)

        return nyTilkjentYtelse.copy(utbetalingsoppdrag = utbetalingsoppdrag,
                                     andelerTilkjentYtelse = gjeldendeAndeler,
                                     sisteAndelIKjede = sisteAndelIKjede)
        //TODO legge til startperiode, sluttperiode, opphørsdato. Se i BA-sak - legges på i konsistensavstemming?
    }

    private fun sisteAndelIKjede(gjeldendeAndeler: List<AndelTilkjentYtelse>,
                                 forrigeTilkjentYtelse: TilkjentYtelse?) =
            (gjeldendeAndeler + listOfNotNull(forrigeTilkjentYtelse?.sisteAndelIKjede))
                    .filter { it.periodeId != null }
                    .filter { it.fraOgMed != NULL_DATO }
                    .maxByOrNull { it.periodeId ?: error("Mangler periodeId") }

    private fun erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse: TilkjentYtelse?) =
            forrigeTilkjentYtelse == null || (forrigeTilkjentYtelseManglerPeriodeOgErNy(forrigeTilkjentYtelse))

    private fun forrigeTilkjentYtelseManglerPeriodeOgErNy(forrigeTilkjentYtelse: TilkjentYtelse): Boolean {
        val utbetalingsoppdrag = forrigeTilkjentYtelse.utbetalingsoppdrag
                                 ?: error("Mangler utbetalingsoppdrag for tilkjentYtelse=${forrigeTilkjentYtelse.id}")
        return utbetalingsoppdrag.utbetalingsperiode.isEmpty() && utbetalingsoppdrag.kodeEndring == NY
    }

    /**
     * Hvis det ikke er noen andeler igjen, må vi opprette en "null-andel" som tar vare på periodeId'en for ytelsestypen
     */
    private fun List<AndelTilkjentYtelse>.ellerNullAndel(nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
                                                         sistePeriodeIdIForrigeKjede: PeriodeId?): List<AndelTilkjentYtelse> {
        return this.ifEmpty {
            listOf(nullAndelTilkjentYtelse(nyTilkjentYtelseMedMetaData.behandlingId, sistePeriodeIdIForrigeKjede))
        }
    }

    private fun lagUtbetalingsperioderForOpprettelse(andeler: List<AndelTilkjentYtelse>,
                                                     tilkjentYtelse: TilkjentYtelseMedMetaData): List<Utbetalingsperiode> {
        return andeler.map {
            lagPeriodeFraAndel(andel = it,
                               type = tilkjentYtelse.stønadstype,
                               eksternBehandlingId = tilkjentYtelse.eksternBehandlingId,
                               vedtaksdato = tilkjentYtelse.vedtaksdato,
                               personIdent = tilkjentYtelse.personIdent)
        }
    }

    private fun lagAndelerMedPeriodeId(andeler: List<AndelTilkjentYtelse>,
                                       sisteOffsetIKjedeOversikt: PeriodeId?,
                                       kildeBehandlingId: UUID): List<AndelTilkjentYtelse> {
        val forrigePeriodeIdIKjede: Long? = sisteOffsetIKjedeOversikt?.gjeldende
        val nestePeriodeIdIKjede = forrigePeriodeIdIKjede?.plus(1) ?: 1

        return andeler.sortedBy { it.fraOgMed }.mapIndexed { index, andel ->
            andel.copy(periodeId = nestePeriodeIdIKjede + index,
                       kildeBehandlingId = kildeBehandlingId,
                       forrigePeriodeId = if (index == 0) forrigePeriodeIdIKjede else nestePeriodeIdIKjede + index - 1)
        }
    }

    private fun sistePeriodeId(tilkjentYtelse: TilkjentYtelse?): PeriodeId? {
        return tilkjentYtelse?.let { ytelse ->
            ytelse.sisteAndelIKjede?.tilPeriodeId() ?:
            ytelse.andelerTilkjentYtelse.filter { it.periodeId != null }.maxByOrNull { it.periodeId!! }?.tilPeriodeId()
        }
    }
}