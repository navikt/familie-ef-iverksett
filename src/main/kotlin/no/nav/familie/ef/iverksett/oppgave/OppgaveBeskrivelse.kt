package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object OppgaveBeskrivelse {
    fun beskrivelseFørstegangsbehandlingInnvilget(
        periode: Pair<LocalDate, LocalDate>,
        vedtak: VedtaksperiodeOvergangsstønad,
    ): String {
        val beskrivelse =
            "Overgangsstønad er innvilget fra ${periode.vedtaksPeriodeToString()}. " +
                "Aktivitet: ${vedtak.aktivitet.beskrivelse()}."

        val hensvisningServicerutine = " Du finner \"Enslig mor eller far - Servicerutiner\" på Navet. Den beskriver hvordan bruker skal følges opp i ulike situasjoner."

        return beskrivelse + hensvisningServicerutine
    }

    fun beskrivelseFørstegangsbehandlingAvslått(vedtaksdato: LocalDate): String = "Søknad om overgangsstønad er avslått i vedtak datert ${vedtaksdato.toReadable()}."

    fun beskrivelseRevurderingInnvilget(
        vedtaksPeriode: Pair<LocalDate, LocalDate>,
        gjeldendeVedtak: VedtaksperiodeOvergangsstønad,
    ): String =
        "Overgangsstønad revurdert. Periode ${vedtaksPeriode.vedtaksPeriodeToString()}. " +
            "Aktivitet: ${gjeldendeVedtak.aktivitet.beskrivelse()}."

    fun beskrivelseRevurderingOpphørt(opphørsdato: LocalDate?): String =
        opphørsdato?.let {
            "Overgangsstønad er stanset fra ${opphørsdato.toReadable()}."
        } ?: "Overgangsstønad er stanset"

    private fun LocalDate.toReadable(): String = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    private fun Pair<LocalDate, LocalDate>.vedtaksPeriodeToString(): String = this.first.toReadable() + " - " + this.second.toReadable()

    fun YearMonth.tilTekst(): String {
        val månedÅrSomTekst = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("nb"))
        return this.format(månedÅrSomTekst)
    }

    private fun AktivitetType.beskrivelse(): String =
        when (this) {
            AktivitetType.MIGRERING -> error("Skal ikke opprette oppfølgningsoppgave for migrering")
            AktivitetType.IKKE_AKTIVITETSPLIKT -> ""
            AktivitetType.BARN_UNDER_ETT_ÅR -> "Barn er under 1 år"
            AktivitetType.FORSØRGER_I_ARBEID -> "Forsørger er i arbeid (§15-6 første ledd)"
            AktivitetType.FORSØRGER_I_UTDANNING -> "Forsørger er i utdanning (§15-6 første ledd)"
            AktivitetType.FORSØRGER_REELL_ARBEIDSSØKER -> "Forsørger er reell arbeidssøker (§15-6 første ledd)"
            AktivitetType.FORSØRGER_ETABLERER_VIRKSOMHET -> "Forsørger etablerer egen virksomhet (§15-6 første ledd)"
            AktivitetType.BARNET_SÆRLIG_TILSYNSKREVENDE -> "Barnet er særlig tilsynskrevende (§15-6 fjerde ledd)"
            AktivitetType.FORSØRGER_MANGLER_TILSYNSORDNING -> "Forsørger mangler tilsynsordning (§15-6 femte ledd)"
            AktivitetType.FORSØRGER_ER_SYK -> "Forsørger er syk (§15-6 femte ledd)"
            AktivitetType.BARNET_ER_SYKT -> "Barnet er sykt (§15-6 femte ledd)"
            AktivitetType.UTVIDELSE_BARNET_SÆRLIG_TILSYNSKREVENDE -> "Barnet er særlig tilsynskrevende (§15-8 tredje ledd)"
            AktivitetType.UTVIDELSE_FORSØRGER_I_UTDANNING -> "Forsørgeren er i utdanning (§15-8 andre ledd)"
            AktivitetType.FORLENGELSE_MIDLERTIDIG_SYKDOM -> "Forsørger eller barnet har en midlertidig sykdom (§15-8 fjerde ledd)"
            AktivitetType.FORLENGELSE_STØNAD_UT_SKOLEÅRET -> "Stønad ut skoleåret (§15-8 andre ledd)"
            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_ARBEID -> "Stønad i påvente av arbeid (§15-8 femte ledd)"
            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_UTDANNING -> "Stønad i påvente av utdanning (§15-8 femte ledd)"
            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_ARBEID_REELL_ARBEIDSSØKER ->
                "Stønad i påvente av arbeid - reell arnbeidssøker (§15-8 femte ledd)"

            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_OPPSTART_KVALIFISERINGSPROGRAM ->
                "Stønad i påvente av oppstart kvalifiseringsprogram"

            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_TILSYNSORDNING -> "Stønad i påvente av tilsynsordning (§15-8 femte ledd)"
        }
}
