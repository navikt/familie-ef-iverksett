package no.nav.familie.ef.iverksett.felles

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.PersonIdentMedHistorikk
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class FamilieIntegrasjonerClient(
        @Qualifier("azure") restOperations: RestOperations,
        @Value("\${FAMILIE_INTEGRASJONER_API_URL}")
        private val integrasjonUri: URI
) : AbstractRestClient(restOperations, "familie.integrasjoner") {

    val logger = LoggerFactory.getLogger(this::class.java)

    private val hentIdenterURI =
            UriComponentsBuilder.fromUri(integrasjonUri).pathSegment(PATH_HENT_IDENTER).build().toUri()

    fun hentIdenter(personident: String, medHistprikk: Boolean): List<PersonIdentMedHistorikk> {
        val uri = UriComponentsBuilder.fromUri(hentIdenterURI).queryParam("historikk", medHistprikk).build().toUri()
        val response = postForEntity<Ressurs<FinnPersonidenterResponse>>(uri, PersonIdent(personident))
        return response.getDataOrThrow().identer
    }

    companion object {

        const val PATH_HENT_IDENTER = "api/personopplysning/v1/identer/ENF"
    }

}