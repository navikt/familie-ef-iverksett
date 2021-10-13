package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandling
import no.nav.familie.kontrakter.felles.tilbakekreving.FinnesBehandlingResponse
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.KanBehandlingOpprettesManueltRespons
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettManueltTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

@Component
class TilbakekrevingClient(@Qualifier("azure") restOperations: RestOperations,
                           @Value("\${FAMILIE_TILBAKE_URL}") private val familieTilbakeUri: URI)
    : AbstractRestClient(restOperations, "familie.tilbakekreving") {

    private val hentForhåndsvisningVarselbrevUri: URI = UriComponentsBuilder.fromUri(familieTilbakeUri)
            .pathSegment("api/dokument/forhandsvis-varselbrev")
            .build()
            .toUri()

    private val opprettTilbakekrevingUri: URI =
            UriComponentsBuilder.fromUri(familieTilbakeUri).pathSegment("api/behandling/v1").build().toUri()

    private val opprettBehandlingManueltUri = UriComponentsBuilder.fromUri(familieTilbakeUri)
            .pathSegment("api/behandling/manuelt/task/v1")
            .build()
            .toUri()

    private fun finnesÅpenBehandlingUri(fagsakId: UUID) = UriComponentsBuilder.fromUri(familieTilbakeUri)
            .pathSegment("api/fagsystem/${Fagsystem.EF}/fagsak/$fagsakId/finnesApenBehandling/v1")
            .build()
            .toUri()

    private fun finnBehandlingerUri(fagsakId: UUID) = UriComponentsBuilder.fromUri(familieTilbakeUri)
            .pathSegment("api/fagsystem/${Fagsystem.EF}/fagsak/$fagsakId/behandlinger/v1")
            .build()
            .toUri()

    private fun kanBehandlingOpprettesManueltUri(fagsakId: UUID, ytelsestype: Ytelsestype) = UriComponentsBuilder.fromUri(
            familieTilbakeUri)
            .pathSegment("api/ytelsestype/$ytelsestype/fagsak/$fagsakId/kanBehandlingOpprettesManuelt/v1")
            .build()
            .toUri()

    fun hentForhåndsvisningVarselbrev(forhåndsvisVarselbrevRequest: ForhåndsvisVarselbrevRequest): ByteArray {
        return postForEntity(hentForhåndsvisningVarselbrevUri,
                             forhåndsvisVarselbrevRequest,
                             HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_PDF) })
    }

    fun opprettBehandling(opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest): String {
        val response: Ressurs<String> = postForEntity(opprettTilbakekrevingUri, opprettTilbakekrevingRequest)
        return response.getDataOrThrow()
    }

    fun opprettBehandlingManuelt(request: OpprettManueltTilbakekrevingRequest) {
        postForEntity<Ressurs<String>>(opprettBehandlingManueltUri, request)
    }

    fun finnesÅpenBehandling(fagsakId: UUID): Boolean {
        val response: Ressurs<FinnesBehandlingResponse> = getForEntity(finnesÅpenBehandlingUri(fagsakId))
        return response.getDataOrThrow().finnesÅpenBehandling
    }

    fun finnBehandlinger(fagsakId: UUID): List<Behandling> {
        val response: Ressurs<List<Behandling>> = getForEntity(finnBehandlingerUri(fagsakId))
        return response.getDataOrThrow()
    }

    fun kanBehandlingOpprettesManuelt(fagsakId: UUID, ytelsestype: Ytelsestype): KanBehandlingOpprettesManueltRespons {
        val response: Ressurs<KanBehandlingOpprettesManueltRespons> =
                getForEntity(kanBehandlingOpprettesManueltUri(fagsakId, ytelsestype))
        return response.getDataOrThrow()
    }

}
