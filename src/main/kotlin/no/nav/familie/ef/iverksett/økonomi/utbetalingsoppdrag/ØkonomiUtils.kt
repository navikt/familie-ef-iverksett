package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse.Companion.disjunkteAndeler
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse.Companion.snittAndeler
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.time.LocalDate
import java.util.UUID

data class PeriodeId(val gjeldende: Long?,
                     val forrige: Long? = null)

fun AndelTilkjentYtelse.tilPeriodeId(): PeriodeId = PeriodeId(this.periodeId, this.forrigePeriodeId)

@Deprecated("Bør erstattes med å gjøre 'stønadFom' og  'stønadTom'  nullable")
val NULL_DATO: LocalDate = LocalDate.MIN

fun nullAndelTilkjentYtelse(kildeBehandlingId: UUID, periodeId: PeriodeId?): AndelTilkjentYtelse =
        AndelTilkjentYtelse(beløp = 0,
                            fraOgMed = NULL_DATO,
                            tilOgMed = NULL_DATO,
                            periodetype = Periodetype.MÅNED,

                            inntekt = 0,
                            samordningsfradrag = 0,
                            inntektsreduksjon = 0,

                            periodeId = periodeId?.gjeldende,
                            kildeBehandlingId = kildeBehandlingId,
                            forrigePeriodeId = periodeId?.forrige)

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
    fun beståendeAndeler(andelerForrigeTilkjentYtelse: List<AndelTilkjentYtelse>,
                         andelerNyTilkjentYtelse: List<AndelTilkjentYtelse>): List<AndelTilkjentYtelse> {
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
    fun andelerTilOpprettelse(andelerNyTilkjentYtelse: List<AndelTilkjentYtelse>,
                              beståendeAndeler: List<AndelTilkjentYtelse>): List<AndelTilkjentYtelse> {
        return beståendeAndeler.maxByOrNull { it.tilOgMed }?.let { sisteBeståendeAndel ->
            andelerNyTilkjentYtelse.filter { it.fraOgMed.isAfter(sisteBeståendeAndel.fraOgMed) }
        } ?: andelerNyTilkjentYtelse
    }

    /**
     * Tar utgangspunkt i forrige tilstand og finner kjede med andeler til opphør og tilhørende opphørsdato
     *
     * @param[andelerForrigeTilkjentYtelse] forrige behandlings tilstand, uten andeler med 0-beløp
     * @param[andelerNyTilkjentYtelse] nåværende tilstand
     * @return utbetalingsperiode for opphør, returnerer null hvis det ikke finnes ett opphørsdato
     */
    fun utbetalingsperiodeForOpphør(forrigeTilkjentYtelse: TilkjentYtelse?,
                                    nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData): Utbetalingsperiode? {
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        validerOpphørsdato(forrigeTilkjentYtelse, nyTilkjentYtelse)
        if (forrigeTilkjentYtelse == null) return null

        val sisteForrigeAndel = forrigeTilkjentYtelse.sisteAndelIKjede
                                ?: andelerUtenNullVerdier(forrigeTilkjentYtelse).lastOrNull()
                                ?: return null // hvis det ikke finnes tidligere andel så kan vi ikke opphøre noe

        val forrigeMaksDato = andelerUtenNullVerdier(forrigeTilkjentYtelse).map { it.tilOgMed }.maxOrNull()

        val opphørsdato = beregnOpphørsdato(forrigeTilkjentYtelse, nyTilkjentYtelse)

        return if (opphørsdato == null || erNyPeriode(forrigeMaksDato, opphørsdato)) {
            null
        } else {
            lagUtbetalingsperiodeForOpphør(sisteForrigeAndel, opphørsdato, nyTilkjentYtelseMedMetaData)
        }
    }

    /**
     * Skal bruke opphørsdato fra tilkjent ytelse hvis den ikke er den samme som forrige opphørsdato
     * Hvis ikke så skal den finne finnOpphørsdato, som gjør en diff mellom tidligere og nye andeler
     *
     * Skal finne opphørsdato til utbetalingsoppdraget
     *
     * Returnerer første endret periode, uavhengig om den er andel med 0-beløp eller ikke
     * Dette for å kunne opphøre perioder bak i tiden, som kan være før perioder som finnes i EF, men som finnes i Infotrygd
     *
     * Hvis forrige kjede inneholder 2 andeler og den nye kjeden endrer i den andre andelen,
     * så skal opphørsdatoet settes til startdato for andre andelen i forrige kjede
     */
    private fun beregnOpphørsdato(forrigeTilkjentYtelse: TilkjentYtelse,
                                  nyTilkjentYtelse: TilkjentYtelse): LocalDate? {
        val forrigeStartdato = forrigeTilkjentYtelse.startdato
        // TODO ulempen med dette er hvis vi bestemmer å sende startdato på alle behandlinger fra ef-sak, då må vi nog flytte denne, eller sjekke om nytt startdato er før første dato på nye perioder?
        if (forrigeStartdato != nyTilkjentYtelse.startdato) return nyTilkjentYtelse.startdato

        val andelerForrigeTilkjentYtelse = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val forrigeAndeler = andelerForrigeTilkjentYtelse.toSet()
        val oppdaterteAndeler = nyTilkjentYtelse.andelerTilkjentYtelse.toSet()

        val førsteEndring = finnOpphørsdato(forrigeAndeler, oppdaterteAndeler)

        if (opphørsdatoErEtterTidligereOpphørsdato(forrigeTilkjentYtelse, førsteEndring, forrigeStartdato)) {
            return null
        }
        return førsteEndring
    }

    /**
     * Hvis tidligere andeler er tomme eller kun nullbeløp
     * Sjekker om første endring er før tidligere startdato eller før forrigeSisteAndel i kjeden
     */
    private fun opphørsdatoErEtterTidligereOpphørsdato(forrigeTilkjentYtelse: TilkjentYtelse,
                                                       førsteEndring: LocalDate?,
                                                       forrigeStartdato: LocalDate?): Boolean {
        val sisteAndelIKjedeTilOgMed = forrigeTilkjentYtelse.sisteAndelIKjede?.fraOgMed
        val tidligereAndeler = forrigeTilkjentYtelse.andelerTilkjentYtelse
        val manglerTidligereAndeler = tidligereAndeler.isEmpty() || tidligereAndeler.singleOrNull()?.erNull() == true
        if (førsteEndring != null && manglerTidligereAndeler
            && ((forrigeStartdato != null && førsteEndring >= forrigeStartdato) ||
                (sisteAndelIKjedeTilOgMed != null && førsteEndring >= sisteAndelIKjedeTilOgMed))) {
            return true
        }
        return false
    }

    /**
     * Nytt opphørsdato må finnes hvis det finnes opphørsdato på tidligere tilkjent ytelse
     * Nytt opphørsdato må være etter forrige opphørsdato, då den inneholder dato for når vi historiskt har første datoet
     * Opphørsdato kan ikke være etter første andel
     */
    private fun validerOpphørsdato(forrigeTilkjentYtelse: TilkjentYtelse?,
                                   nyTilkjentYtelse: TilkjentYtelse) {
        val nyMinDato = nyTilkjentYtelse.andelerTilkjentYtelse.minOfOrNull { it.fraOgMed }
        val forrigeOpphørsdato = forrigeTilkjentYtelse?.startdato
        val nyOpphørsdato = nyTilkjentYtelse.startdato
        if (forrigeOpphørsdato != null) {
            if (nyOpphørsdato == null) {
                error("Må ha med opphørsdato hvis man har tidligere opphørsdato")
            } else if (nyOpphørsdato > forrigeOpphørsdato) {
                error("Nytt opphørsdato=$nyOpphørsdato kan ikke være etter forrigeOpphørsdato=$forrigeOpphørsdato")
            }
        }

        if (nyOpphørsdato != null && forrigeTilkjentYtelse == null) {
            error("Kan ikke opphøre noe når det ikke finnes en tidligere behandling")
        }

        if (nyOpphørsdato != null && nyMinDato != null && nyMinDato.isBefore(nyOpphørsdato)) {
            error("Kan ikke sette opphør etter dato på første perioden")
        }

    }

    /**
     * Skal finne opphørsdato til utbetalingsoppdraget
     *
     * Returnerer første endret periode, uavhengig om den er andel med 0-beløp eller ikke
     * Dette for å kunne opphøre perioder bak i tiden, som kan være før perioder som finnes i EF, men som finnes i Infotrygd
     *
     * Hvis forrige kjede inneholder 2 andeler og den nye kjeden endrer i den andre andelen,
     * så skal opphørsdatoet settes til startdato for andre andelen i forrige kjede
     */
    private fun finnOpphørsdato(forrigeAndeler: Set<AndelTilkjentYtelse>,
                                oppdaterteAndeler: Set<AndelTilkjentYtelse>): LocalDate? {
        val førsteEndring = finnDatoForFørsteEndredeAndel(forrigeAndeler, oppdaterteAndeler)
        val førsteDatoIForrigePeriode = forrigeAndeler.minByOrNull { it.fraOgMed }?.fraOgMed
        val førsteDatoNyePerioder = oppdaterteAndeler.minOfOrNull { it.fraOgMed }
        if (førsteDatoNyePerioder != null && førsteDatoIForrigePeriode != null &&
            førsteDatoNyePerioder.isBefore(førsteDatoIForrigePeriode)) {
            return førsteDatoNyePerioder
        }
        return førsteEndring
    }

    private fun finnDatoForFørsteEndredeAndel(andelerForrigeTilkjentYtelse: Set<AndelTilkjentYtelse>,
                                              andelerNyTilkjentYtelse: Set<AndelTilkjentYtelse>) =
            andelerForrigeTilkjentYtelse.disjunkteAndeler(andelerNyTilkjentYtelse)
                    .minByOrNull { it.fraOgMed }?.fraOgMed

    /**
     * Sjekker om den nye endringen er etter maks datot for tidligere perioder
     */
    private fun erNyPeriode(forrigeMaksDato: LocalDate?, førsteEndring: LocalDate) =
            forrigeMaksDato != null && førsteEndring.isAfter(forrigeMaksDato)
}

