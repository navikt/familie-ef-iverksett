package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping(path = ["/api/oppgave/opprett"])
@ProtectedWithClaims(issuer = "azuread")
@Profile("dev", "local")
class OppgaveTestController(
    val oppgaveClient: OppgaveClient,
    val familieIntegrasjonerClient: FamilieIntegrasjonerClient
) {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun opprettOppgave(@RequestBody data: IverksettDto) {
        val iverksett = data.toDomain()
        val enhetsnummer = familieIntegrasjonerClient.hentNavEnhetForOppfølging(iverksett.søker.personIdent)

        val opprettOppgaveRequest =
            OpprettOppgaveRequest(
                ident = OppgaveIdentV2(ident = iverksett.søker.personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
                saksId = iverksett.fagsak.eksternId.toString(),
                tema = Tema.ENF,
                oppgavetype = Oppgavetype.VurderHenvendelse,
                fristFerdigstillelse = LocalDate.now(),
                beskrivelse = oppgaveBeskrivelse(data),
                enhetsnummer = enhetsnummer?.enhetId,
                behandlingstema = Behandlingstema.fromValue(iverksett.fagsak.stønadstype.name.toLowerCase().capitalize()).value,
                tilordnetRessurs = null,
                behandlesAvApplikasjon = "familie-ef-sak"
            )
        oppgaveClient.opprettOppgave(opprettOppgaveRequest)
    }

    private fun oppgaveBeskrivelse(iverksettDto: IverksettDto): String {
        val gjeldendeVedtak = iverksettDto.vedtak.vedtaksperioder.sortedBy { it.fraOgMed }.last()
        return "${iverksettDto.fagsak.stønadstype.name.enumToReadable()} er innvilget fra " +
                "${gjeldendeVedtak.fraOgMed.toReadable()} - ${gjeldendeVedtak.tilOgMed.toReadable()}. " +
                "Aktivitetsplikt: ${gjeldendeVedtak.aktivitet.name.enumToReadable()}" +
                ". Periodetype: ${gjeldendeVedtak.periodeType.name.enumToReadable()}. Saken ligger i ny løsning."
    }

    fun String.enumToReadable(): String {
        return this.replace("_", " ").toLowerCase().capitalize()
    }
    fun LocalDate.toReadable(): String {
        return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
}