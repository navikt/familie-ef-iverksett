package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.util.VirkedagerProvider
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class OppgaveService(private val oppgaveClient: OppgaveClient,
                     private val familieIntegrasjonerClient: FamilieIntegrasjonerClient) {

    fun opprettOppgave(iverksett: Iverksett) {

        val enhetsnummer = familieIntegrasjonerClient.hentNavEnhetForOppfølging(iverksett.søker.personIdent)

        val opprettOppgaveRequest =
            OpprettOppgaveRequest(
                ident = OppgaveIdentV2(ident = iverksett.søker.personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
                saksId = iverksett.fagsak.eksternId.toString(),
                tema = Tema.ENF,
                oppgavetype = Oppgavetype.VurderHenvendelse,
                fristFerdigstillelse = fristFerdigstillelse(),
                beskrivelse = oppgaveBeskrivelse(iverksett),
                enhetsnummer = enhetsnummer?.enhetId,
                behandlingstema = Behandlingstema.fromValue(iverksett.fagsak.stønadstype.name.lowercase(Locale.getDefault())
                                                                .replaceFirstChar { it.uppercase() }).value,
                tilordnetRessurs = null,
                behandlesAvApplikasjon = "familie-ef-sak"
            )
        oppgaveClient.opprettOppgave(opprettOppgaveRequest)
    }

    private fun oppgaveBeskrivelse(iverksett: Iverksett): String {
        val gjeldendeVedtak = iverksett.vedtak.vedtaksperioder.maxByOrNull { it.fraOgMed }!!
        return "${iverksett.fagsak.stønadstype.name.enumToReadable()} er innvilget fra " +
                "${gjeldendeVedtak.fraOgMed.toReadable()} - ${gjeldendeVedtak.tilOgMed.toReadable()}. " +
                "Aktivitetsplikt: ${gjeldendeVedtak.aktivitet.name.enumToReadable()}" +
                ". Periodetype: ${gjeldendeVedtak.periodeType.name.enumToReadable()}. Saken ligger i ny løsning."
    }

    fun String.enumToReadable(): String {
        return this.replace("_", " ").lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
    }

    fun LocalDate.toReadable(): String {
        return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    fun fristFerdigstillelse(daysToAdd: Long = 0): LocalDate {
        var dateTime = LocalDateTime.now().plusDays(daysToAdd)

        if (dateTime.hour >= 14) {
            dateTime = dateTime.plusDays(1)
        }

        return VirkedagerProvider.nesteVirkedag(dateTime.toLocalDate())
    }

}