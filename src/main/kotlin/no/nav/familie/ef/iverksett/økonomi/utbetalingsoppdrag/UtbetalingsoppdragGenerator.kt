package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsgeneratorHelper.FagsystemEF
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsgeneratorHelper.YtelsestypeEF
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerTilOpprettelse
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerUtenNullVerdier
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.beståendeAndeler
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.utbetalingsperiodeForOpphør
import no.nav.familie.felles.utbetalingsgenerator.Utbetalingsgenerator
import no.nav.familie.felles.utbetalingsgenerator.domain.AndelData
import no.nav.familie.felles.utbetalingsgenerator.domain.Behandlingsinformasjon
import no.nav.familie.felles.utbetalingsgenerator.domain.IdentOgType
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag.KodeEndring
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsperiode
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import no.nav.familie.felles.utbetalingsgenerator.domain.Ytelsestype as YtelsestypeUG

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
        val personIdent = nyTilkjentYtelseMedMetaData.personIdent
        val ytelseType = nyTilkjentYtelseMedMetaData.stønadstype.tilYtelseType()

        val forrigeAndeler =
            (forrigeTilkjentYtelse?.andelerTilkjentYtelse ?: emptyList())
                .map { it.tilAndelData(id = UUID.randomUUID().toString(), personIdent = personIdent, ytelseType = ytelseType) }
        val nyeAndelerPåId =
            nyTilkjentYtelseMedMetaData.tilkjentYtelse.andelerTilkjentYtelse.associateBy { UUID.randomUUID().toString() }

        val nyeAndeler = tilAndelData(nyeAndelerPåId, personIdent, ytelseType)
        val forrigeSisteAndelIKjede = forrigeTilkjentYtelse?.sisteAndelIKjede
        val sisteAndelPerKjede = mapTilSisteAndelPerKjede(forrigeSisteAndelIKjede, personIdent, ytelseType)

        val beregnetUtbetalingsoppdrag =
            Utbetalingsgenerator().lagUtbetalingsoppdrag(
                behandlingsinformasjon =
                    behandlingsinformasjon(
                        nyTilkjentYtelseMedMetaData,
                        erGOmregning,
                        forrigeTilkjentYtelse,
                    ),
                nyeAndeler = nyeAndeler,
                forrigeAndeler = forrigeAndeler,
                sisteAndelPerKjede = sisteAndelPerKjede,
            )
        val beregnedeAndelerPåId = beregnetUtbetalingsoppdrag.andeler.associateBy { it.id }
        val gjeldendeAndeler =
            nyeAndelerPåId.entries.map { (id, andel) ->
                if (andel.harNullBeløp()) {
                    // TODO vi kan vurdere om vi bare skal filtrere vekk de etter att man har laget utbetalingsoppdraget, då vil de ikke bli sendt med som "forrige andeler" i neste utbetalingsoppdrag
                    andel
                } else {
                    val beregnetAndel = beregnedeAndelerPåId.getValue(id)
                    andel.copy(
                        periodeId = beregnetAndel.periodeId,
                        forrigePeriodeId = beregnetAndel.forrigePeriodeId,
                        kildeBehandlingId = UUID.fromString(beregnetAndel.kildeBehandlingId),
                    )
                }
            }

        return nyTilkjentYtelseMedMetaData.tilkjentYtelse.copy(
            utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
            andelerTilkjentYtelse = gjeldendeAndeler,
            sisteAndelIKjede = nySisteAndelIKjede(gjeldendeAndeler, forrigeSisteAndelIKjede),
        )
    }

    private fun tilAndelData(
        nyeAndelerPåId: Map<String, AndelTilkjentYtelse>,
        personIdent: String,
        ytelseType: YtelsestypeUG,
    ) = nyeAndelerPåId.map { (id, andel) ->
        andel.tilAndelData(id = id, personIdent = personIdent, ytelseType = ytelseType)
    }

    /**
     * Id brukes ikke til noe spesielt for siste andel i kjeden, men er en del av [AndelData]
     * Settes til noe random.
     */
    private val idSisteAndelIKjeden = "SISTE_ANDEL_I_KJEDEN"

    /**
     * Utbetalingsgeneratorn ønsker en map med ident og type, då den har støtte for å sende inn flere personer som brukes av Barnetrygd
     */
    private fun mapTilSisteAndelPerKjede(
        forrigeSisteAndelIKjede: AndelTilkjentYtelse?,
        personIdent: String,
        ytelseType: YtelsestypeUG,
    ) = forrigeSisteAndelIKjede?.tilAndelData(idSisteAndelIKjeden, personIdent, ytelseType)
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
        fagsystem = nyTilkjentYtelseMedMetaData.stønadstype.tilFagsystem(),
        personIdent = nyTilkjentYtelseMedMetaData.personIdent,
        vedtaksdato = nyTilkjentYtelseMedMetaData.vedtaksdato,
        opphørAlleKjederFra = opphørFra(forrigeTilkjentYtelse, nyTilkjentYtelseMedMetaData),
        utbetalesTil = null,
        erGOmregning = erGOmregning,
    )

    /**
     * Brukes for å løse de casene hvor man midlertidig opphører tilbake i tid hvis du har noe i infotrygd fra før.
     * |-----50------| [INFOTRYGD]
     *        |-----100------| FØRSTEGANGSBEHANDLING
     *    |--0--|---100--|     REVURDERING (midlertidig opphør)
     *    |--0-------->        REVURDERING (Opphør)
     * I dette spesialtilfellet vil opphøret skje FØR første andel i forrige tilkjent ytelse,
     * og gjeldende tilkjent ytelse starter med et opphør. Da har vi ingen andeler å hente startdato fra,
     * og bruker spesialtilfellet med startmåned for å finne første opphørsdato
     */
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

    private fun AndelTilkjentYtelse.tilAndelData(
        id: String,
        personIdent: String,
        ytelseType: YtelsestypeUG,
    ): AndelData =
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
            utbetalingsgrad = if (ytelseType == YtelsestypeEF.OVERGANGSSTØNAD) this.utbetalingsgrad() else null,
        )

    private fun StønadType.tilFagsystem(): FagsystemEF =
        when (this) {
            StønadType.OVERGANGSSTØNAD -> FagsystemEF.OVERGANGSSTØNAD
            StønadType.BARNETILSYN -> FagsystemEF.BARNETILSYN
            StønadType.SKOLEPENGER -> FagsystemEF.SKOLEPENGER
        }

    private fun StønadType.tilYtelseType(): YtelsestypeUG =
        when (this) {
            StønadType.OVERGANGSSTØNAD -> YtelsestypeEF.OVERGANGSSTØNAD
            StønadType.BARNETILSYN -> YtelsestypeEF.BARNETILSYN
            StønadType.SKOLEPENGER -> YtelsestypeEF.SKOLEPENGER
        }

    @Deprecated("Fases ut til fordel for lagTilkjentYtelseMedUtbetalingsoppdragNy. Brukes kun for simuleringskontroll")
    fun lagTilkjentYtelseMedUtbetalingsoppdrag(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        forrigeTilkjentYtelse: TilkjentYtelse? = null,
        erGOmregning: Boolean = false,
    ): TilkjentYtelse {
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        val andelerNyTilkjentYtelse = andelerUtenNullVerdier(nyTilkjentYtelse)
        val andelerForrigeTilkjentYtelse = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val sistePeriodeIdIForrigeKjede = sistePeriodeId(forrigeTilkjentYtelse)

        val beståendeAndeler =
            beståendeAndeler(
                andelerForrigeTilkjentYtelse,
                andelerNyTilkjentYtelse,
                nyTilkjentYtelse.startmåned,
                forrigeTilkjentYtelse?.startmåned,
            )

        val andelerTilOpprettelse = andelerTilOpprettelse(andelerNyTilkjentYtelse, beståendeAndeler)

        val andelerTilOpprettelseMedPeriodeId =
            lagAndelerMedPeriodeId(
                andelerTilOpprettelse,
                sistePeriodeIdIForrigeKjede,
                nyTilkjentYtelseMedMetaData.behandlingId,
            )

        val utbetalingsperioderSomOpprettes =
            lagUtbetalingsperioderForOpprettelse(
                andeler = andelerTilOpprettelseMedPeriodeId,
                tilkjentYtelse = nyTilkjentYtelseMedMetaData,
            )

        val utbetalingsperiodeSomOpphøres =
            utbetalingsperiodeForOpphør(forrigeTilkjentYtelse, nyTilkjentYtelseMedMetaData)

        val utbetalingsperioder =
            (utbetalingsperioderSomOpprettes + utbetalingsperiodeSomOpphøres)
                .filterNotNull()
                .sortedBy { it.periodeId }
        val utbetalingsoppdrag =
            Utbetalingsoppdrag(
                saksbehandlerId = nyTilkjentYtelseMedMetaData.saksbehandlerId,
                kodeEndring = if (erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse)) KodeEndring.NY else KodeEndring.ENDR,
                fagSystem = nyTilkjentYtelseMedMetaData.stønadstype.tilKlassifisering(),
                saksnummer = nyTilkjentYtelseMedMetaData.eksternFagsakId.toString(),
                aktoer = nyTilkjentYtelseMedMetaData.personIdent,
                utbetalingsperiode = utbetalingsperioder,
                gOmregning = erGOmregning,
            )

        val gjeldendeAndeler =
            (beståendeAndeler + andelerTilOpprettelseMedPeriodeId)
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
    ) = (gjeldendeAndeler + listOfNotNull(forrigeTilkjentYtelse?.sisteAndelIKjede))
        .filter { it.periodeId != null }
        .filter { it.periode.fomDato != LocalDate.MIN }
        .maxByOrNull { it.periodeId ?: error("Mangler periodeId") }

    private fun erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse: TilkjentYtelse?) =
        forrigeTilkjentYtelse == null || (forrigeTilkjentYtelseManglerPeriodeOgErNy(forrigeTilkjentYtelse))

    private fun forrigeTilkjentYtelseManglerPeriodeOgErNy(forrigeTilkjentYtelse: TilkjentYtelse): Boolean {
        val utbetalingsoppdrag =
            forrigeTilkjentYtelse.utbetalingsoppdrag
                ?: error("Mangler utbetalingsoppdrag for tilkjentYtelse=${forrigeTilkjentYtelse.id}")
        return utbetalingsoppdrag.utbetalingsperiode.isEmpty() && utbetalingsoppdrag.kodeEndring == KodeEndring.NY
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

    private fun sistePeriodeId(tilkjentYtelse: TilkjentYtelse?): PeriodeId? = tilkjentYtelse?.sisteAndelIKjede?.tilPeriodeId()
}
