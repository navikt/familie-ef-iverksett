package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OppgaveClient(
    @Qualifier("azure") restOperations: RestOperations,
    @Value("\${FAMILIE_INTEGRASJONER_API_URL}") private val integrasjonUrl: String,
) : AbstractRestClient(restOperations, "familie.integrasjoner") {

    val oppgaveUrl = "$integrasjonUrl/api/oppgave"

    fun hentOppgaver(finnOppgaveRequest: FinnOppgaveRequest): List<Oppgave> {
        val opprettOppgaveUri = URI.create("$oppgaveUrl/v4")
        val response =
            postForEntity<Ressurs<FinnOppgaveResponseDto>>(
                opprettOppgaveUri,
                finnOppgaveRequest,
                HttpHeaders().medContentTypeJsonUTF8()
            )
        return response.getDataOrThrow().oppgaver
    }

    fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): Long? {

        val opprettOppgaveUri = URI.create("$oppgaveUrl/opprett")
        val response =
            postForEntity<Ressurs<OppgaveResponse>>(
                opprettOppgaveUri,
                opprettOppgaveRequest,
                HttpHeaders().medContentTypeJsonUTF8()
            )
        return response.data?.oppgaveId
    }

    fun finnOppgaveMedId(oppgaveId: Long): Oppgave {
        val response = getForEntity<Ressurs<Oppgave>>(
            URI.create("$oppgaveUrl/$oppgaveId"),
            HttpHeaders().medContentTypeJsonUTF8()
        )
        return response.getDataOrThrow()
    }

    fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        val response = getForEntity<Ressurs<FinnMappeResponseDto>>(
            UriComponentsBuilder.fromUri(URI.create("$oppgaveUrl/mappe/sok"))
                .queryParams(finnMappeRequest.toQueryParams())
                .build()
                .toUri()
        )
        return response.getDataOrThrow()
    }

    fun oppdaterOppgave(oppgave: Oppgave): Long {
        val response = patchForEntity<Ressurs<OppgaveResponse>>(
            URI.create("$oppgaveUrl/${oppgave.id!!}/oppdater"),
            oppgave,
            HttpHeaders().medContentTypeJsonUTF8()
        )
        return response.getDataOrThrow().oppgaveId
    }
}
