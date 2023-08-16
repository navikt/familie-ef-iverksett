package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerTilOpprettelse
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerUtenNullVerdier
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.beståendeAndeler
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.utbetalingsperiodeForOpphør
import no.nav.familie.felles.utbetalingsgenerator.Utbetalingsgenerator
import no.nav.familie.felles.utbetalingsgenerator.domain.AndelData
import no.nav.familie.felles.utbetalingsgenerator.domain.Behandlingsinformasjon
import no.nav.familie.felles.utbetalingsgenerator.domain.IdentOgType
import no.nav.familie.felles.utbetalingsgenerator.domain.YtelseType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.ENDR
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.NY
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.time.LocalDate
import java.time.YearMonth
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
    fun lagTilkjentYtelseMedUtbetalingsoppdragNy(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        forrigeTilkjentYtelse: TilkjentYtelse? = null,
        erGOmregning: Boolean = false,
    ): TilkjentYtelse {
        val stønadstype = nyTilkjentYtelseMedMetaData.stønadstype
        val personIdent = nyTilkjentYtelseMedMetaData.personIdent
        val ytelseType = stønadstype.tilYtelseType()
        var counter = 0
        fun nextId() = (++counter).toString()

        val forrigeAndeler = (forrigeTilkjentYtelse?.andelerTilkjentYtelse ?: emptyList())
            .map { it.tilAndelData(id = nextId(), personIdent = personIdent, ytelseType = ytelseType) }
        val nyeAndelerPåId = nyTilkjentYtelseMedMetaData.tilkjentYtelse.andelerTilkjentYtelse.associateBy { nextId() }

        val nyeAndeler = tilAndelData(nyeAndelerPåId, personIdent, ytelseType)
        val forrigeSisteAndelIKjede = forrigeTilkjentYtelse?.sisteAndelIKjede
        val sisteAndelPerKjede = mapTilSisteAndelPerKjede(forrigeSisteAndelIKjede, personIdent, ytelseType)

        val beregnetUtbetalingsoppdrag = Utbetalingsgenerator().lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon(
                nyTilkjentYtelseMedMetaData,
                erGOmregning,
                forrigeTilkjentYtelse,
            ),
            nyeAndeler = nyeAndeler,
            forrigeAndeler = forrigeAndeler,
            sisteAndelPerKjede = sisteAndelPerKjede,
        )
        val beregnedeAndelerPåId = beregnetUtbetalingsoppdrag.andeler.associateBy { it.id }
        val gjeldendeAndeler = nyeAndelerPåId.entries.map {
            if (it.value.harNullBeløp()) {
                // TODO vi kan vurdere om vi bare skal filtrere vekk de etter att man har laget utbetalingsoppdraget, då vil de ikke bli sendt med som "forrige andeler" i neste utbetalingsoppdrag
                it.value
            } else {
                val beregnetAndel = beregnedeAndelerPåId.getValue(it.key)
                it.value.copy(
                    periodeId = beregnetAndel.periodeId,
                    forrigePeriodeId = beregnetAndel.forrigePeriodeId,
                    kildeBehandlingId = UUID.fromString(beregnetAndel.kildeBehandlingId),
                )
            }
        }
        // TODO settes kanskje ikke kildeBehandlingId i ef-sak? Settes den då nå kanskje feil hvis man avkorter en periode?
        val copy = nyTilkjentYtelseMedMetaData.tilkjentYtelse.copy(
            utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
            andelerTilkjentYtelse = gjeldendeAndeler,
            sisteAndelIKjede = nySisteAndelIKjede(gjeldendeAndeler, forrigeSisteAndelIKjede),
        )
        return copy
    }

    private fun tilAndelData(
        nyeAndelerPåId: Map<String, AndelTilkjentYtelse>,
        personIdent: String,
        ytelseType: YtelseType,
    ) = nyeAndelerPåId.map { (id, andel) ->
        andel.tilAndelData(id = id, personIdent = personIdent, ytelseType = ytelseType)
    }

    /**
     * Utbetalingsgeneratorn ønsker en map med ident og type, då den har støtte for å sende inn flere personer som brukes av Barnetrygd
     */
    private fun mapTilSisteAndelPerKjede(
        forrigeSisteAndelIKjede: AndelTilkjentYtelse?,
        personIdent: String,
        ytelseType: YtelseType,
    ) = forrigeSisteAndelIKjede?.tilAndelData("-1", personIdent, ytelseType)
        ?.let { mapOf(IdentOgType(personIdent, ytelseType) to it) } ?: emptyMap()

    private fun behandlingsinformasjon(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        erGOmregning: Boolean,
        forrigeTilkjentYtelse: TilkjentYtelse?,
    ) = Behandlingsinformasjon(
        saksbehandlerId = nyTilkjentYtelseMedMetaData.saksbehandlerId,
        behandlingId = nyTilkjentYtelseMedMetaData.behandlingId.toString(),
        eksternBehandlingId = nyTilkjentYtelseMedMetaData.eksternBehandlingId,
        eksternFagsakId = nyTilkjentYtelseMedMetaData.eksternFagsakId,
        ytelse = nyTilkjentYtelseMedMetaData.stønadstype.tilYtelsestype(),
        personIdent = nyTilkjentYtelseMedMetaData.personIdent,
        vedtaksdato = nyTilkjentYtelseMedMetaData.vedtaksdato,
        opphørFra = opphørFra(forrigeTilkjentYtelse, nyTilkjentYtelseMedMetaData),
        utbetalesTil = null,
        erGOmregning = erGOmregning,
    )

    private fun opphørFra(
        forrigeTilkjentYtelse: TilkjentYtelse?,
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
    ): YearMonth? {
        if (forrigeTilkjentYtelse == null) return null
        val forrigeStartdato = forrigeTilkjentYtelse.startmåned
        val nyStartdato = nyTilkjentYtelseMedMetaData.tilkjentYtelse.startmåned
        val førsteAndelDato =
            nyTilkjentYtelseMedMetaData.tilkjentYtelse.andelerTilkjentYtelse.minOfOrNull { it.periode.fom }
        if (nyStartdato < forrigeStartdato && (førsteAndelDato == null || nyStartdato < førsteAndelDato)) {
            return nyStartdato
        }
        return null
    }

    private fun nySisteAndelIKjede(
        gjeldendeAndeler: List<AndelTilkjentYtelse>,
        forrigeSisteAndelIKjede: AndelTilkjentYtelse?,
    ): AndelTilkjentYtelse? {
        val sisteAndelINyKjede = gjeldendeAndeler.filterNot { it.harNullBeløp() }.maxByOrNull { it.periodeId!! }
        return if (
            forrigeSisteAndelIKjede != null && sisteAndelINyKjede != null &&
            forrigeSisteAndelIKjede.periodeId!! < sisteAndelINyKjede.periodeId!!
        ) {
            sisteAndelINyKjede
        } else {
            forrigeSisteAndelIKjede ?: sisteAndelINyKjede
        }
    }

    private fun AndelTilkjentYtelse.tilAndelData(id: String, personIdent: String, ytelseType: YtelseType): AndelData =
        AndelData(
            id = id,
            fom = periode.fom,
            tom = periode.tom,
            beløp = beløp,
            personIdent = personIdent,
            type = ytelseType,
            periodeId = periodeId,
            forrigePeriodeId = forrigePeriodeId,
            kildeBehandlingId = kildeBehandlingId?.toString(),
            utbetalingsgrad = if (ytelseType == YtelseType.OVERGANGSSTØNAD) this.utbetalingsgrad() else null,
        )

    private fun StønadType.tilYtelsestype(): Ytelsestype = when (this) {
        StønadType.OVERGANGSSTØNAD -> Ytelsestype.OVERGANGSSTØNAD
        StønadType.BARNETILSYN -> Ytelsestype.BARNETILSYN
        StønadType.SKOLEPENGER -> Ytelsestype.SKOLEPENGER
    }

    private fun StønadType.tilYtelseType(): YtelseType = when (this) {
        StønadType.OVERGANGSSTØNAD -> YtelseType.OVERGANGSSTØNAD
        StønadType.BARNETILSYN -> YtelseType.BARNETILSYN
        StønadType.SKOLEPENGER -> YtelseType.SKOLEPENGER
    }

    fun lagTilkjentYtelseMedUtbetalingsoppdrag(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        forrigeTilkjentYtelse: TilkjentYtelse? = null,
        erGOmregning: Boolean = false,
    ): TilkjentYtelse {
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        val andelerNyTilkjentYtelse = andelerUtenNullVerdier(nyTilkjentYtelse)
        val andelerForrigeTilkjentYtelse = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val sistePeriodeIdIForrigeKjede = sistePeriodeId(forrigeTilkjentYtelse)

        val beståendeAndeler = beståendeAndeler(
            andelerForrigeTilkjentYtelse,
            andelerNyTilkjentYtelse,
            nyTilkjentYtelse.startmåned,
            forrigeTilkjentYtelse?.startmåned,
        )

        val andelerTilOpprettelse = andelerTilOpprettelse(andelerNyTilkjentYtelse, beståendeAndeler)

        val andelerTilOpprettelseMedPeriodeId = lagAndelerMedPeriodeId(
            andelerTilOpprettelse,
            sistePeriodeIdIForrigeKjede,
            nyTilkjentYtelseMedMetaData.behandlingId,
        )

        val utbetalingsperioderSomOpprettes = lagUtbetalingsperioderForOpprettelse(
            andeler = andelerTilOpprettelseMedPeriodeId,
            tilkjentYtelse = nyTilkjentYtelseMedMetaData,
        )

        val utbetalingsperiodeSomOpphøres =
            utbetalingsperiodeForOpphør(forrigeTilkjentYtelse, nyTilkjentYtelseMedMetaData)

        val utbetalingsperioder = (utbetalingsperioderSomOpprettes + utbetalingsperiodeSomOpphøres)
            .filterNotNull()
            .sortedBy { it.periodeId }
        val utbetalingsoppdrag =
            Utbetalingsoppdrag(
                saksbehandlerId = nyTilkjentYtelseMedMetaData.saksbehandlerId,
                kodeEndring = if (erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse)) NY else ENDR,
                fagSystem = nyTilkjentYtelseMedMetaData.stønadstype.tilKlassifisering(),
                saksnummer = nyTilkjentYtelseMedMetaData.eksternFagsakId.toString(),
                aktoer = nyTilkjentYtelseMedMetaData.personIdent,
                utbetalingsperiode = utbetalingsperioder,
                gOmregning = erGOmregning,
            )

        val gjeldendeAndeler = (beståendeAndeler + andelerTilOpprettelseMedPeriodeId)
            .ellerNullAndel(nyTilkjentYtelseMedMetaData, sistePeriodeIdIForrigeKjede)

        val sisteAndelIKjede = sisteAndelIKjede(gjeldendeAndeler, forrigeTilkjentYtelse)

        return nyTilkjentYtelse.copy(
            utbetalingsoppdrag = utbetalingsoppdrag,
            andelerTilkjentYtelse = gjeldendeAndeler,
            sisteAndelIKjede = sisteAndelIKjede,
        )
        // TODO legge til startperiode, sluttperiode, opphørsdato. Se i BA-sak - legges på i konsistensavstemming?
    }

    private fun sisteAndelIKjede(
        gjeldendeAndeler: List<AndelTilkjentYtelse>,
        forrigeTilkjentYtelse: TilkjentYtelse?,
    ) =
        (gjeldendeAndeler + listOfNotNull(forrigeTilkjentYtelse?.sisteAndelIKjede))
            .filter { it.periodeId != null }
            .filter { it.periode.fomDato != LocalDate.MIN }
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
    private fun List<AndelTilkjentYtelse>.ellerNullAndel(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        sistePeriodeIdIForrigeKjede: PeriodeId?,
    ): List<AndelTilkjentYtelse> {
        return this.ifEmpty {
            listOf(nullAndelTilkjentYtelse(nyTilkjentYtelseMedMetaData.behandlingId, sistePeriodeIdIForrigeKjede))
        }
    }

    private fun lagUtbetalingsperioderForOpprettelse(
        andeler: List<AndelTilkjentYtelse>,
        tilkjentYtelse: TilkjentYtelseMedMetaData,
    ): List<Utbetalingsperiode> {
        return andeler.map {
            lagPeriodeFraAndel(
                andel = it,
                type = tilkjentYtelse.stønadstype,
                eksternBehandlingId = tilkjentYtelse.eksternBehandlingId,
                vedtaksdato = tilkjentYtelse.vedtaksdato,
                personIdent = tilkjentYtelse.personIdent,
            )
        }
    }

    private fun lagAndelerMedPeriodeId(
        andeler: List<AndelTilkjentYtelse>,
        sisteOffsetIKjedeOversikt: PeriodeId?,
        kildeBehandlingId: UUID,
    ): List<AndelTilkjentYtelse> {
        val forrigePeriodeIdIKjede: Long? = sisteOffsetIKjedeOversikt?.gjeldende
        val nestePeriodeIdIKjede = forrigePeriodeIdIKjede?.plus(1) ?: 1

        return andeler.sortedBy { it.periode }.mapIndexed { index, andel ->
            andel.copy(
                periodeId = nestePeriodeIdIKjede + index,
                kildeBehandlingId = kildeBehandlingId,
                forrigePeriodeId = if (index == 0) forrigePeriodeIdIKjede else nestePeriodeIdIKjede + index - 1,
            )
        }
    }

    private fun sistePeriodeId(tilkjentYtelse: TilkjentYtelse?): PeriodeId? =
        tilkjentYtelse?.sisteAndelIKjede?.tilPeriodeId()
}
