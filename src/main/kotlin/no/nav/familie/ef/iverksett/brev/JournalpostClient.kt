package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.log.NavHttpHeaders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class JournalpostClient(
    @Qualifier("azure") restOperations: RestOperations,
    @Value("\${FAMILIE_INTEGRASJONER_API_URL}")
    private val integrasjonUri: URI
) : AbstractPingableRestClient(restOperations, "journalpost") {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override val pingUri: URI = URI("/ping")
    private val dokarkivUri: URI = UriComponentsBuilder.fromUri(integrasjonUri).pathSegment("api/arkiv").build().toUri()
    private val journalPostUri: URI = UriComponentsBuilder.fromUri(integrasjonUri).pathSegment("api/journalpost").build().toUri()
    private val distribuerDokumentUri: URI =
        UriComponentsBuilder.fromUri(integrasjonUri).pathSegment("api/dist/v1").build().toUri()

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        return postForEntity<Ressurs<List<Journalpost>>>(journalPostUri, journalposterForBrukerRequest).data
            ?: error("Kunne ikke journalposter for for ${journalposterForBrukerRequest.brukerId.id}")
    }

    fun arkiverDokument(arkiverDokumentRequest: ArkiverDokumentRequest, saksbehandler: String?): ArkiverDokumentResponse {
        return postForEntity<Ressurs<ArkiverDokumentResponse>>(
            URI.create("$dokarkivUri/v4/"),
            arkiverDokumentRequest,
            headerMedSaksbehandler(saksbehandler)
        ).data
            ?: error("Kunne ikke arkivere dokument med fagsakid ${arkiverDokumentRequest.fagsakId}")
    }

    fun distribuerBrev(journalpostId: String, distribusjonstype: Distribusjonstype): String {
        logger.info("Kaller dokdist-tjeneste for journalpost=$journalpostId")

        val journalpostRequest = DistribuerJournalpostRequest(
            journalpostId = journalpostId,
            bestillendeFagsystem = Fagsystem.EF,
            dokumentProdApp = "FAMILIE_EF_SAK",
            distribusjonstype = distribusjonstype
        )

        return postForEntity<Ressurs<String>>(
            distribuerDokumentUri,
            journalpostRequest,
            HttpHeaders().medContentTypeJsonUTF8()
        ).getDataOrThrow()
    }

    private fun headerMedSaksbehandler(saksbehandler: String?): HttpHeaders {
        val httpHeaders = HttpHeaders()
        if (saksbehandler != null) {
            httpHeaders.set(NavHttpHeaders.NAV_USER_ID.asString(), saksbehandler)
        }
        return httpHeaders
    }
}
