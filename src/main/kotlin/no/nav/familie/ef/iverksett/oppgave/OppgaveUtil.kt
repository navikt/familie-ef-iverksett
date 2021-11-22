package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.innvilgetFom
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.innvilgetTom
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.toReadable
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.util.VirkedagerProvider
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object OppfølgingsoppgaveBeskrivelse {

    fun beskrivelseFørstegangsbehandling(iverksett: Iverksett): String {
        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> "Overgangsstønad er innvilget fra " +
                                         "${iverksett.totalVedtaksPeriode().first.toReadable()} - ${iverksett.totalVedtaksPeriode().second.toReadable()}. " +
                                         "Aktivitet: ${iverksett.gjeldendeVedtak().aktivitet.name.enumToReadable()}"
            Vedtaksresultat.AVSLÅTT -> "Overgangsstønad avslått. Vedtaksdato : " +
                                       "${iverksett.vedtak.vedtakstidspunkt.toLocalDate().toReadable()}"
            else -> throw error("Kan ikke opprette oppfølgingsoppgave for førstegangsbehandling for opphør")
        }
    }

    fun beskrivelseRevurdering(iverksett: Iverksett) : String {
        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> "Overgangsstønad er innvilget fra " +
    }

    fun beskrivelseRevurdering(iverksett: Iverksett) : String {
        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> "Overgangsstønad er innvilget fra " +
                                         "${iverksett.totalVedtaksPeriode().first.toReadable()} - ${iverksett.totalVedtaksPeriode().second.toReadable()}. " +
                                         "Aktivitet: ${iverksett.gjeldendeVedtak().aktivitet.name.enumToReadable()}"
            Vedtaksresultat.AVSLÅTT -> "Overgangsstønad avslått. Vedtaksdato : " +
                                       "${iverksett.vedtak.vedtakstidspunkt.toLocalDate().toReadable()}"
            else -> throw error("Skal ikke opprette beskrivelse for oppfølgingsoppgave ved opphør")
        }

    }
    private fun String.enumToReadable(): String {
        return this.replace("_", " ").lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
    }

    private fun LocalDate.toReadable(): String {
        return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    private fun fristFerdigstillelse(daysToAdd: Long = 0): LocalDate {
        var dateTime = LocalDateTime.now().plusDays(daysToAdd)

        if (dateTime.hour >= 14) {
            dateTime = dateTime.plusDays(1)
        }

        return VirkedagerProvider.nesteVirkedag(dateTime.toLocalDate())
    }

    private fun Iverksett.totalVedtaksPeriode(): Pair<LocalDate, LocalDate> =
            Pair(this.vedtak.vedtaksperioder.minOf { it.fraOgMed }, this.vedtak.vedtaksperioder.maxOf { it.tilOgMed })

    private fun Iverksett.gjeldendeVedtak() = this.vedtak.vedtaksperioder.maxByOrNull { it.fraOgMed }!!

}

object OppgaveUtil {

    fun opprettOppgaveRequest(iverksett: Iverksett, enhetsnummer: Enhet?): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
                ident = OppgaveIdentV2(ident = iverksett.søker.personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
                saksId = iverksett.fagsak.eksternId.toString(),
                tema = Tema.ENF,
                oppgavetype = Oppgavetype.VurderHenvendelse,
                fristFerdigstillelse = fristFerdigstillelse(),
                beskrivelse = oppgaveBeskrivelse(iverksett),
                enhetsnummer = enhetsnummer?.enhetId,
                behandlingstema = Behandlingstema
                        .fromValue(iverksett.fagsak.stønadstype.name.lowercase(Locale.getDefault())
                                           .replaceFirstChar { it.uppercase() }).value,
                tilordnetRessurs = null,
                behandlesAvApplikasjon = "familie-ef-sak"
        )
    }

    /**
     * E.g : Overgangsstønad er innvilget fra 01.01.2021 - 31.12.2022. Aktivitet: Forsørger er i arbeid (§15-6 første ledd)
     */
    fun førstegangsbehandlingBeskrivelse(iverksett: Iverksett): String {
        return "Overgangsstønad er innvilget fra ${iverksett.innvilgetFom()} - ${iverksett.innvilgetTom()}. "
    }


    private fun AktivitetType.toReadable() {
        return "Aktivitetsplikt: ${this.name.enumToReadable()}" +
               ". Periodetype: ${gjeldendeVedtak.periodeType.name.enumToReadable()}. Saken ligger i ny løsning."
    }

    private fun oppgaveBeskrivelseOvergangsstønad(iverksett: Iverksett): String {
        return "Overgangsstønad er innvilget fra " +
               "${iverksett.innvilgetFom().toReadable()} - ${iverksett.innvilgetTom().toReadable()}. " +
               "Aktivitetsplikt: ${gjeldendeVedtak.aktivitet.name.enumToReadable()}" +
               ". Periodetype: ${gjeldendeVedtak.periodeType.name.enumToReadable()}. Saken ligger i ny løsning."
    }


}
