package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksett.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksett.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Lager mal for generering av utbetalingsperioder med tilpasset setting av verdier basert på parametre
 *
 * @param[vedtak] for vedtakdato og opphørsdato hvis satt
 * @param[erEndringPåEksisterendePeriode] ved true vil oppdrag sette asksjonskode ENDR på linje og ikke referere bakover
 * @return mal med tilpasset lagPeriodeFraAndel
 */
data class UtbetalingsperiodeMal(val tilkjentYtelse: TilkjentYtelseMedMetaData,
                                 val erEndringPåEksisterendePeriode: Boolean = false) {

    /**
     * Lager utbetalingsperioder som legges på utbetalingsoppdrag. En utbetalingsperiode tilsvarer linjer hos økonomi
     *
     * @param[andel] andel som skal mappes til periode
     * @param[opphørKjedeFom] fom-dato fra tidligste periode i kjede med endring
     * @return Periode til utbetalingsoppdrag
     */
    fun lagPeriodeFraAndel(andel: AndelTilkjentYtelse,
                           type: StønadType,
                           behandlingId: Long,
                           opphørKjedeFom: LocalDate? = null): Utbetalingsperiode =
            Utbetalingsperiode(erEndringPåEksisterendePeriode = erEndringPåEksisterendePeriode,
                               opphør = if (erEndringPåEksisterendePeriode) Opphør(opphørKjedeFom!!) else null,
                               forrigePeriodeId = andel.forrigePeriodeId,
                               periodeId = andel.periodeId!!,
                               datoForVedtak = tilkjentYtelse.vedtaksdato,
                               klassifisering = type.tilKlassifisering(),
                               vedtakdatoFom = andel.periodebeløp.fraOgMed,
                               vedtakdatoTom = andel.periodebeløp.tilOgMed,
                               sats = BigDecimal(andel.periodebeløp.beløp),
                               satsType = Utbetalingsperiode.SatsType.MND,
                               utbetalesTil = tilkjentYtelse.personIdent,
                               behandlingId = behandlingId)

}
