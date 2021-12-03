package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object OppfølgingsoppgaveBeskrivelse {

    fun beskrivelseFørstegangsbehandlingInnvilget(periode: Pair<LocalDate, LocalDate>, vedtak: Vedtaksperiode): String {
        return "Overgangsstønad er innvilget fra ${periode.vedtaksPeriodeToString()}. " +
                "Aktivitet: ${vedtak.aktivitet.name.enumToReadable()}."
    }

    fun beskrivelseFørstegangsbehandlingAvslått(vedtaksdato: LocalDate): String {
        return "Søknad om overgangsstønad er avslått i vedtak datert ${vedtaksdato.toReadable()}."
    }

    fun beskrivelseRevurderingInnvilget(vedtaksPeriode: Pair<LocalDate, LocalDate>, gjeldendeVedtak: Vedtaksperiode): String {
        return "Overgangsstønad revurdert. Periode ${vedtaksPeriode.vedtaksPeriodeToString()}." +
                "Aktivitet: ${gjeldendeVedtak.aktivitet.name.enumToReadable()}."
    }

    fun beskrivelseRevurderingOpphørt(vedtaksTidspunkt: LocalDateTime): String {
        return "Overgangsstønad er stanset fra ${vedtaksTidspunkt.toLocalDate().toReadable()}."
    }

    private fun String.enumToReadable(): String {
        return this.replace("_", " ").lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
    }

    private fun LocalDate.toReadable(): String {
        return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    private fun Pair<LocalDate, LocalDate>.vedtaksPeriodeToString(): String {
        return this.first.toReadable() + " - " + this.second.toReadable()
    }

}