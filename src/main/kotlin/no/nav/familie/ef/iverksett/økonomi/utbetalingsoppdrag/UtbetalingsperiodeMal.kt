package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.felles.utbetalingsgenerator.domain.Opphør
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsperiode
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.math.BigDecimal
import java.time.LocalDate

/**
 * * Lager utbetalingsperioder som legges på utbetalingsoppdrag. En utbetalingsperiode tilsvarer linjer hos økonomi
 *
 * @param[andel] andel som skal mappes til periode
 * @param[opphørKjedeFom] fom-dato fra tidligste periode i kjede med endring
 * @param[erEndringPåEksisterendePeriode] ved true vil oppdrag sette asksjonskode ENDR på linje og ikke referere bakover
 * @return Periode til utbetalingsoppdrag
 */
fun lagPeriodeFraAndel(
    andel: AndelTilkjentYtelse,
    type: StønadType,
    eksternBehandlingId: Long,
    vedtaksdato: LocalDate,
    personIdent: String,
    opphørKjedeFom: LocalDate? = null,
    erEndringPåEksisterendePeriode: Boolean = false,
) =
    Utbetalingsperiode(
        erEndringPåEksisterendePeriode = erEndringPåEksisterendePeriode,
        opphør = if (erEndringPåEksisterendePeriode) Opphør(opphørKjedeFom!!) else null,
        forrigePeriodeId = andel.forrigePeriodeId,
        periodeId = andel.periodeId!!,
        datoForVedtak = vedtaksdato,
        klassifisering = type.tilKlassifisering(),
        vedtakdatoFom = andel.periode.fomDato,
        vedtakdatoTom = andel.periode.tomDato,
        sats = BigDecimal(andel.beløp),
        satsType = mapSatstype(type),
        utbetalesTil = personIdent,
        behandlingId = eksternBehandlingId,
        utbetalingsgrad = andel.utbetalingsgrad(),
    )

fun lagUtbetalingsperiodeForOpphør(
    sisteAndelIKjede: AndelTilkjentYtelse,
    opphørKjedeFom: LocalDate,
    tilkjentYtelse: TilkjentYtelseMedMetaData,
): Utbetalingsperiode {
    return lagPeriodeFraAndel(
        andel = sisteAndelIKjede,
        eksternBehandlingId = tilkjentYtelse.eksternBehandlingId,
        type = tilkjentYtelse.stønadstype,
        personIdent = tilkjentYtelse.personIdent,
        vedtaksdato = tilkjentYtelse.vedtaksdato,
        opphørKjedeFom = opphørKjedeFom,
        erEndringPåEksisterendePeriode = true,
    )
}

fun mapSatstype(stønadstype: StønadType) = when (stønadstype) {
    StønadType.OVERGANGSSTØNAD,
    StønadType.BARNETILSYN,
    -> Utbetalingsperiode.SatsType.MND
    StønadType.SKOLEPENGER -> Utbetalingsperiode.SatsType.ENG
    else -> error("Støtter ikke periodetype=$stønadstype")
}
