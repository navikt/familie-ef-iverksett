package no.nav.familie.ef.iverksett.journalføring

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.Ressurs

import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class JournalpostClient(
    @Qualifier("azure") restOperations: RestOperations,
    @Value("\${FAMILIE_INTEGRASJONER_API_URL}")
    private val dokarkiv: URI
) : AbstractPingableRestClient(restOperations, "journalpost") {

    override val pingUri: URI = URI("/ping")
    private val dokarkivUri: URI = UriComponentsBuilder.fromUri(dokarkiv).pathSegment("api/arkiv").build().toUri()

    fun arkiverDokument(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        return postForEntity<Ressurs<ArkiverDokumentResponse>>(
            URI.create("${dokarkivUri}/v4/"),
            arkiverDokumentRequest
        ).data
            ?: error("Kunne ikke arkivere dokument med fagsakid ${arkiverDokumentRequest.fagsakId}")
    }
}