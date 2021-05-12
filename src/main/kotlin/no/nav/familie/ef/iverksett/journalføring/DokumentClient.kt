package no.nav.familie.ef.iverksett.journalføring

import no.nav.familie.ef.iverksett.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import no.nav.familie.kontrakter.felles.getDataOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokumentClient(@Qualifier("azure") restOperations: RestOperations,
                     @Value("\${FAMILIE_INTEGRASJONER_API_URL}")
                     private val integrasjonUri: URI) : AbstractPingableRestClient(restOperations, "vedtaksbrev") {

    val logger = LoggerFactory.getLogger(this::class.java)
    override val pingUri: URI = URI("/ping")
    private val PATH_DISTRIBUER_DOKUMENT = "api/dist/v1"

    val distribuerDokumentUri: URI =
            UriComponentsBuilder.fromUri(integrasjonUri).pathSegment(PATH_DISTRIBUER_DOKUMENT).build().toUri()

    fun distribuerBrev(journalpostId: String): String {
        logger.info("Kaller dokdist-tjeneste for journalpost=$journalpostId")

        val journalpostRequest = DistribuerJournalpostRequest(journalpostId = journalpostId,
                                                              bestillendeFagsystem = Fagsystem.EF,
                                                              dokumentProdApp = "FAMILIE_EF_SAK")

        return postForEntity<Ressurs<String>>(distribuerDokumentUri,
                                              journalpostRequest,
                                              HttpHeaders().medContentTypeJsonUTF8()).getDataOrThrow()
    }
}