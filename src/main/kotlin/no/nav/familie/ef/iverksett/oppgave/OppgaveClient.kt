package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.exception.IntegrasjonException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OppgaveClient(
    @Qualifier("integrasjonerRestClient") private val restClient: RestClient,
    @Value("\${FAMILIE_INTEGRASJONER_API_URL}") private val integrasjonUrl: String,
) {
    val oppgaveUrl = "$integrasjonUrl/api/oppgave"

    fun finnMapper(
        enhetsnummer: String,
        limit: Int,
    ): FinnMappeResponseDto {
        val uri =
            UriComponentsBuilder
                .fromUri(URI.create(oppgaveUrl))
                .pathSegment("mappe", "sok")
                .queryParam("enhetsnr", enhetsnummer)
                .queryParam("limit", limit)
                .build()
                .toUri()
        val response =
            restClient
                .get()
                .uri(uri)
                .retrieve()
                .body<Ressurs<FinnMappeResponseDto>>()!!
        return response.data ?: error("Kunne ikke hente mapper for enhetsnummer=$enhetsnummer")
    }

    fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): Long? {
        val opprettOppgaveUri = URI.create("$oppgaveUrl/opprett")
        val response =
            restClient
                .post()
                .uri(opprettOppgaveUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(opprettOppgaveRequest)
                .retrieve()
                .body<Ressurs<OppgaveResponse>>()!!
        return response.data?.oppgaveId
    }

    fun oppdaterOppgave(oppgave: Oppgave): Long {
        val response =
            restClient
                .patch()
                .uri(URI.create("$oppgaveUrl/${oppgave.id}/oppdater"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(oppgave)
                .retrieve()
                .body<Ressurs<OppgaveResponse>>()!!
        return response.getDataOrThrow().oppgaveId
    }

    fun finnOppgaveMedId(oppgaveId: Long): Oppgave {
        val uri = URI.create("$oppgaveUrl/$oppgaveId")
        val respons =
            restClient
                .get()
                .uri(uri)
                .retrieve()
                .body<Ressurs<Oppgave>>()!!
        return pakkUtRespons(respons, uri, "finnOppgaveMedId")
    }

    private fun <T> pakkUtRespons(
        respons: Ressurs<T>,
        uri: URI?,
        metode: String,
    ): T {
        val data = respons.data
        if (respons.status == Ressurs.Status.SUKSESS && data != null) {
            return data
        } else if (respons.status == Ressurs.Status.SUKSESS) {
            throw IntegrasjonException("Ressurs har status suksess, men mangler data")
        } else {
            throw IntegrasjonException(
                "Respons fra $metode feilet med status=${respons.status} melding=${respons.melding}",
                null,
                uri,
                data,
            )
        }
    }
}
