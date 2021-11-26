package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class OppgaveClient(
        @Qualifier("azure") restOperations: RestOperations,
        @Value("\${FAMILIE_INTEGRASJONER_API_URL}") private val integrasjonUrl: String,
) : AbstractRestClient(restOperations, "familie.integrasjoner") {

    val oppgaveUrl = "$integrasjonUrl/api/oppgave"

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

}

