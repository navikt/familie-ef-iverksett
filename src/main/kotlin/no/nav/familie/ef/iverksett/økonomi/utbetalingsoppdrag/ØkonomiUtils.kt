package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse.Companion.disjunkteAndeler
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse.Companion.snittAndeler
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.time.LocalDate
import java.util.UUID

data class PeriodeId(
    val gjeldende: Long?,
    val forrige: Long? = null
)

fun AndelTilkjentYtelse.tilPeriodeId(): PeriodeId = PeriodeId(this.periodeId, this.forrigePeriodeId)

fun nullAndelTilkjentYtelse(kildeBehandlingId: UUID, periodeId: PeriodeId?): AndelTilkjentYtelse =
    AndelTilkjentYtelse(
        beløp = 0,
        fraOgMed = LocalDate.MIN,
        tilOgMed = LocalDate.MIN,
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

        val førsteEndring = finnDatoForFørsteEndredeAndel(forrigeAndeler, oppdaterteAndeler)
        val består =
            if (førsteEndring != null)
                forrigeAndeler.snittAndeler(oppdaterteAndeler).filter { it.fraOgMed.isBefore(førsteEndring) }
            else forrigeAndeler
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
        return beståendeAndeler.maxByOrNull { it.tilOgMed }?.let { sisteBeståendeAndel ->
            andelerNyTilkjentYtelse.filter { it.fraOgMed.isAfter(sisteBeståendeAndel.fraOgMed) }
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
        val forrigeMaksDato = forrigeAndeler.map { it.tilOgMed }.maxOrNull()
        val nyeAndeler = andelerUtenNullVerdier(nyTilkjentYtelse)

        val skalOpphøreFørTidligereStartdato = nyTilkjentYtelse.startdato < forrigeTilkjentYtelse.startdato
        if (skalOpphøreFørTidligereStartdato) {
            return lagUtbetalingsperiodeForOpphør(sisteForrigeAndel, nyTilkjentYtelse.startdato, nyTilkjentYtelseMedMetaData)
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
        opphørsdato: LocalDate?,
        forrigeTilkjentYtelse: TilkjentYtelse,
        forrigeAndeler: List<AndelTilkjentYtelse>
    ) =
        forrigeAndeler.isEmpty() && opphørsdato != null && opphørsdato >= forrigeTilkjentYtelse.startdato

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
        val nyMinDato = nyTilkjentYtelse.andelerTilkjentYtelse.minOfOrNull { it.fraOgMed }
        val forrigeStartdato = forrigeTilkjentYtelse?.startdato
        val nyStartdato = nyTilkjentYtelse.startdato
        if (forrigeStartdato != null) {
            if (nyStartdato > forrigeStartdato) {
                error("Nytt startdato=$nyStartdato kan ikke være etter forrigeStartdato=$forrigeStartdato")
            }
        }
        if (gammelVersjon && forrigeTilkjentYtelse == null && nyTilkjentYtelse.andelerTilkjentYtelse.isEmpty()) {
            error("Kan ikke opphøre noe når det ikke finnes en tidligere behandling")
        }
        if (nyMinDato != null && nyMinDato.isBefore(nyStartdato)) {
            error("Kan ikke sette opphør etter dato på første perioden")
        }

        if (gammelVersjon) {
            validerOpphørOg0Andeler(forrigeTilkjentYtelse, nyStartdato, forrigeStartdato)
        }
    }

    private fun validerOpphørOg0Andeler(
        forrigeTilkjentYtelse: TilkjentYtelse?,
        nyStartdato: LocalDate?,
        forrigeStartdato: LocalDate?
    ) {
        val harOpphørEllerOpphørFørForrigeTilkjentYtelse =
            nyStartdato != null && (forrigeStartdato == null || (nyStartdato < forrigeStartdato))
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
    ): LocalDate? {
        val førsteEndring = finnDatoForFørsteEndredeAndel(forrigeAndeler, oppdaterteAndeler)
        val førsteDatoIForrigePeriode = forrigeAndeler.minByOrNull { it.fraOgMed }?.fraOgMed
        val førsteDatoNyePerioder = oppdaterteAndeler.minOfOrNull { it.fraOgMed }
        if (førsteDatoNyePerioder != null && førsteDatoIForrigePeriode != null &&
            førsteDatoNyePerioder.isBefore(førsteDatoIForrigePeriode)
        ) {
            return førsteDatoNyePerioder
        }
        return førsteEndring
    }

    private fun finnDatoForFørsteEndredeAndel(
        andelerForrigeTilkjentYtelse: Set<AndelTilkjentYtelse>,
        andelerNyTilkjentYtelse: Set<AndelTilkjentYtelse>
    ) =
        andelerForrigeTilkjentYtelse.disjunkteAndeler(andelerNyTilkjentYtelse)
            .minByOrNull { it.fraOgMed }?.fraOgMed

    /**
     * Sjekker om den nye endringen er etter maks datot for tidligere perioder
     */
    private fun erNyPeriode(forrigeMaksDato: LocalDate?, førsteEndring: LocalDate) =
        forrigeMaksDato != null && førsteEndring.isAfter(forrigeMaksDato)
}
