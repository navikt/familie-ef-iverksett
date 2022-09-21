package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse.Companion.disjunkteAndeler
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse.Companion.snittAndeler
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.felles.Månedsperiode
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class PeriodeId(
    val gjeldende: Long?,
    val forrige: Long? = null
)

fun AndelTilkjentYtelse.tilPeriodeId(): PeriodeId = PeriodeId(this.periodeId, this.forrigePeriodeId)

fun nullAndelTilkjentYtelse(kildeBehandlingId: UUID, periodeId: PeriodeId?): AndelTilkjentYtelse =
    AndelTilkjentYtelse(
        beløp = 0,
        periode = Månedsperiode(LocalDate.MIN, LocalDate.MIN),
        inntekt = 0,
        samordningsfradrag = 0,
        inntektsreduksjon = 0,

        periodeId = periodeId?.gjeldende,
        kildeBehandlingId = kildeBehandlingId,
        forrigePeriodeId = periodeId?.forrige
    )

object ØkonomiUtils {

    fun andelerUtenNullVerdier(tilkjentYtelse: TilkjentYtelse?): List<AndelTilkjentYtelse> =
        tilkjentYtelse?.andelerTilkjentYtelse?.filter { !it.erNull() } ?: emptyList()

    /**
     * Lager oversikt over siste andel i hver kjede som finnes uten endring i oppdatert tilstand.
     * Vi må opphøre og eventuelt gjenoppbygge hver kjede etter denne. Må ta vare på andel og ikke kun offset da
     * filtrering av oppdaterte andeler senere skjer før offset blir satt.
     *
     * @param[andelerForrigeTilkjentYtelse] forrige behandlings tilstand
     * @param[andelerNyTilkjentYtelse] nåværende tilstand
     * @return liste med bestående andeler
     */
    fun beståendeAndeler(
        andelerForrigeTilkjentYtelse: List<AndelTilkjentYtelse>,
        andelerNyTilkjentYtelse: List<AndelTilkjentYtelse>
    ): List<AndelTilkjentYtelse> {
        val forrigeAndeler = andelerForrigeTilkjentYtelse.toSet()
        val oppdaterteAndeler = andelerNyTilkjentYtelse.toSet()

        val førsteEndring = finnMånedForFørsteEndredeAndel(forrigeAndeler, oppdaterteAndeler)
        val består =
            if (førsteEndring != null) {
                forrigeAndeler.snittAndeler(oppdaterteAndeler).filter { it.periode.fom < førsteEndring }
            } else {
                forrigeAndeler
            }
        return består.sortedBy { it.periodeId }
    }

    /**
     * Tar utgangspunkt i ny tilstand og finner andeler som må bygges opp (nye, endrede og bestående etter første endring)
     *
     * @param[andelerNyTilkjentYtelse] ny tilstand
     * @param[beståendeAndeler] andeler man må bygge opp etter
     * @return andeler som må opprettes, hvis det ikke er noen beståendeAndeler returneres oppdatertKjede
     */
    fun andelerTilOpprettelse(
        andelerNyTilkjentYtelse: List<AndelTilkjentYtelse>,
        beståendeAndeler: List<AndelTilkjentYtelse>
    ): List<AndelTilkjentYtelse> {
        return beståendeAndeler.maxByOrNull { it.periode }?.let { sisteBeståendeAndel ->
            andelerNyTilkjentYtelse.filter { it.periode.fom > sisteBeståendeAndel.periode.fom }
        } ?: andelerNyTilkjentYtelse
    }

    /**
     * Tar utgangspunkt i forrige tilstand og finner kjede med andeler til opphør og tilhørende opphørsdato
     *
     * @param[forrigeTilkjentYtelse] forrige tilkjente ytelse
     * @param[nyTilkjentYtelseMedMetaData] nåværende tilstand
     * @return utbetalingsperiode for opphør, returnerer null hvis det ikke finnes en opphørsdato
     */
    fun utbetalingsperiodeForOpphør(
        forrigeTilkjentYtelse: TilkjentYtelse?,
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData
    ): Utbetalingsperiode? {
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        validerStartdato(forrigeTilkjentYtelse, nyTilkjentYtelse)

        // hvis det ikke finnes tidligere andel så kan vi ikke opphøre noe
        val sisteForrigeAndel = forrigeTilkjentYtelse?.sisteAndelIKjede ?: return null
        val forrigeAndeler = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val forrigeMaksDato = forrigeAndeler.maxOfOrNull { it.periode.tom }
        val nyeAndeler = andelerUtenNullVerdier(nyTilkjentYtelse)

        val skalOpphøreFørTidligereStartdato = nyTilkjentYtelse.startmåned < forrigeTilkjentYtelse.startmåned
        if (skalOpphøreFørTidligereStartdato) {
            return lagUtbetalingsperiodeForOpphør(
                sisteForrigeAndel,
                nyTilkjentYtelse.startmåned,
                nyTilkjentYtelseMedMetaData
            )
        }

        val opphørsdato = finnOpphørsdato(forrigeAndeler.toSet(), nyeAndeler.toSet())

        return if (harIngenAndelerÅOpphøre(opphørsdato, forrigeTilkjentYtelse, forrigeAndeler) ||
            opphørsdato == null ||
            erNyPeriode(forrigeMaksDato, opphørsdato)
        ) {
            null
        } else {
            lagUtbetalingsperiodeForOpphør(sisteForrigeAndel, opphørsdato, nyTilkjentYtelseMedMetaData)
        }
    }

    /**
     * Når tidligere andeler er empty, og opphørsdato er etter forrige sitt opphørsdato trenger vi ikke å opphøre noe
     */
    private fun harIngenAndelerÅOpphøre(
        opphørsmåned: YearMonth?,
        forrigeTilkjentYtelse: TilkjentYtelse,
        forrigeAndeler: List<AndelTilkjentYtelse>
    ) =
        forrigeAndeler.isEmpty() && opphørsmåned != null && opphørsmåned >= forrigeTilkjentYtelse.startmåned

    /**
     * Ny opphørsdato må finnes hvis det finnes startdato på tidligere tilkjent ytelse
     * Ny opphørsdato må være etter forrige opphørsdato, då den inneholder dato for når vi historiskt har første datoet
     * Opphørsdato kan ikke være etter første andel
     */
    private fun validerStartdato(
        forrigeTilkjentYtelse: TilkjentYtelse?,
        nyTilkjentYtelse: TilkjentYtelse,
        gammelVersjon: Boolean = false
    ) {
        val nyMinDato = nyTilkjentYtelse.andelerTilkjentYtelse.minOfOrNull { it.periode.fom }
        val forrigeStartmåned = forrigeTilkjentYtelse?.startmåned
        val nyStartmåned = nyTilkjentYtelse.startmåned
        if (forrigeStartmåned != null) {
            if (nyStartmåned > forrigeStartmåned) {
                error("Nytt startdato=$nyStartmåned kan ikke være etter forrigeStartdato=$forrigeStartmåned")
            }
        }
        if (gammelVersjon && forrigeTilkjentYtelse == null && nyTilkjentYtelse.andelerTilkjentYtelse.isEmpty()) {
            error("Kan ikke opphøre noe når det ikke finnes en tidligere behandling")
        }
        if (nyMinDato != null && nyMinDato.isBefore(nyStartmåned)) {
            error("Kan ikke sette opphør etter dato på første perioden")
        }

        if (gammelVersjon) {
            validerOpphørOg0Andeler(forrigeTilkjentYtelse, nyStartmåned, forrigeStartmåned)
        }
    }

    private fun validerOpphørOg0Andeler(
        forrigeTilkjentYtelse: TilkjentYtelse?,
        nyStartmåned: YearMonth?,
        forrigeStartmåned: YearMonth?
    ) {
        val harOpphørEllerOpphørFørForrigeTilkjentYtelse =
            nyStartmåned != null && (forrigeStartmåned == null || (nyStartmåned < forrigeStartmåned))
        val harForrigeTilkjentYtelseUtenBeløp =
            forrigeTilkjentYtelse != null && andelerUtenNullVerdier(forrigeTilkjentYtelse).isEmpty()
        if (harOpphørEllerOpphørFørForrigeTilkjentYtelse && harForrigeTilkjentYtelseUtenBeløp) {
            error("Kan ikke opphøre før tidligere opphør når det finnes en tidligere tilkjent ytelse uten andeler")
        }
    }

    /**
     * Skal finne opphørsdato til utbetalingsoppdraget
     *
     * Returnerer første endret periode, uavhengig om den er andel med 0-beløp eller ikke
     * Dette for å kunne opphøre perioder bak i tiden, som kan være før perioder som finnes i EF, men som finnes i Infotrygd
     *
     * Hvis forrige kjede inneholder 2 andeler og den nye kjeden endrer i den andre andelen,
     * så skal opphørsdato settes til opphørsdato for andre andelen i forrige kjede
     */
    private fun finnOpphørsdato(
        forrigeAndeler: Set<AndelTilkjentYtelse>,
        oppdaterteAndeler: Set<AndelTilkjentYtelse>
    ): YearMonth? {
        val førsteEndring = finnMånedForFørsteEndredeAndel(forrigeAndeler, oppdaterteAndeler)
        val førsteMånedIForrigePeriode = forrigeAndeler.minOfOrNull { it.periode.fom }
        val førsteMånedNyePerioder = oppdaterteAndeler.minOfOrNull { it.periode.fom }
        if (førsteMånedNyePerioder != null && førsteMånedIForrigePeriode != null &&
            førsteMånedNyePerioder < førsteMånedIForrigePeriode
        ) {
            return førsteMånedNyePerioder
        }
        return førsteEndring
    }

    private fun finnMånedForFørsteEndredeAndel(
        andelerForrigeTilkjentYtelse: Set<AndelTilkjentYtelse>,
        andelerNyTilkjentYtelse: Set<AndelTilkjentYtelse>
    ) =
        andelerForrigeTilkjentYtelse.disjunkteAndeler(andelerNyTilkjentYtelse)
            .minOfOrNull { it.periode.fom }

    /**
     * Sjekker om den nye endringen er etter maks datot for tidligere perioder
     */
    private fun erNyPeriode(forrigeMaksDato: YearMonth?, førsteEndring: YearMonth) =
        forrigeMaksDato != null && førsteEndring > forrigeMaksDato
}
