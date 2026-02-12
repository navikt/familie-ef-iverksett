package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.exception.IntegrasjonException
import no.nav.familie.ef.iverksett.util.medContentTypeJsonUTF8
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.restklient.client.AbstractRestClient
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
        val response = getForEntity<Ressurs<FinnMappeResponseDto>>(uri)
        return response.data ?: error("Kunne ikke hente mapper for enhetsnummer=$enhetsnummer")
    }

    fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): Long? {
        val opprettOppgaveUri = URI.create("$oppgaveUrl/opprett")
        val response =
            postForEntity<Ressurs<OppgaveResponse>>(
                opprettOppgaveUri,
                opprettOppgaveRequest,
                HttpHeaders().medContentTypeJsonUTF8(),
            )
        return response.data?.oppgaveId
    }

    fun oppdaterOppgave(oppgave: Oppgave): Long {
        val response =
            patchForEntity<Ressurs<OppgaveResponse>>(
                URI.create("$oppgaveUrl/${oppgave.id}/oppdater"),
                oppgave,
                HttpHeaders().medContentTypeJsonUTF8(),
            )
        return response.getDataOrThrow().oppgaveId
    }

    fun finnOppgaveMedId(oppgaveId: Long): Oppgave {
        val uri = URI.create("$oppgaveUrl/$oppgaveId")

        val respons = getForEntity<Ressurs<Oppgave>>(uri)
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
