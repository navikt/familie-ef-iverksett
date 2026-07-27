package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandling
import no.nav.familie.kontrakter.felles.tilbakekreving.FinnesBehandlingResponse
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.KanBehandlingOpprettesManueltRespons
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class TilbakekrevingClient(
    @Qualifier("tilbakekrevingRestClient")
    private val restClient: RestClient,
    @Value("\${FAMILIE_TILBAKE_URL}") private val familieTilbakeUri: URI,
) {
    private val hentForhåndsvisningVarselbrevUri: URI =
        UriComponentsBuilder
            .fromUri(familieTilbakeUri)
            .pathSegment("api/dokument/forhandsvis-varselbrev")
            .build()
            .toUri()

    private val opprettTilbakekrevingUri: URI =
        UriComponentsBuilder
            .fromUri(familieTilbakeUri)
            .pathSegment("api/behandling/v1")
            .build()
            .toUri()

    private val opprettBehandlingManueltUri =
        UriComponentsBuilder
            .fromUri(familieTilbakeUri)
            .pathSegment("api/behandling/manuelt/task/v1")
            .build()
            .toUri()

    private fun finnesÅpenBehandlingUri(fagsakId: Long) =
        UriComponentsBuilder
            .fromUri(familieTilbakeUri)
            .pathSegment("api/fagsystem/${Fagsystem.EF}/fagsak/$fagsakId/finnesApenBehandling/v1")
            .build()
            .toUri()

    private fun finnBehandlingerUri(fagsakId: Long) =
        UriComponentsBuilder
            .fromUri(familieTilbakeUri)
            .pathSegment("api/fagsystem/${Fagsystem.EF}/fagsak/$fagsakId/behandlinger/v1")
            .build()
            .toUri()

    private fun kanBehandlingOpprettesManueltUri(
        fagsakId: Long,
        ytelsestype: Ytelsestype,
    ) = UriComponentsBuilder
        .fromUri(familieTilbakeUri)
        .pathSegment("api", "ytelsestype", ytelsestype.name, "fagsak", fagsakId.toString(), "kanBehandlingOpprettesManuelt", "v1")
        .encode()
        .build()
        .toUri()

    fun hentForhåndsvisningVarselbrev(forhåndsvisVarselbrevRequest: ForhåndsvisVarselbrevRequest): ByteArray =
        restClient
            .post()
            .uri(hentForhåndsvisningVarselbrevUri)
            .accept(MediaType.APPLICATION_PDF)
            .body(forhåndsvisVarselbrevRequest)
            .retrieve()
            .body<ByteArray>()!!

    fun opprettBehandling(opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest) {
        restClient
            .post()
            .uri(opprettTilbakekrevingUri)
            .body(opprettTilbakekrevingRequest)
            .retrieve()
            .toBodilessEntity()
    }

    fun finnesÅpenBehandling(fagsakId: Long): Boolean {
        val response =
            restClient
                .get()
                .uri(finnesÅpenBehandlingUri(fagsakId))
                .retrieve()
                .body<Ressurs<FinnesBehandlingResponse>>()!!
        return response.getDataOrThrow().finnesÅpenBehandling
    }

    fun finnBehandlinger(fagsakId: Long): List<Behandling> {
        val response =
            restClient
                .get()
                .uri(finnBehandlingerUri(fagsakId))
                .retrieve()
                .body<Ressurs<List<Behandling>>>()!!
        return response.getDataOrThrow()
    }

    fun kanBehandlingOpprettesManuelt(
        fagsakId: Long,
        ytelsestype: Ytelsestype,
    ): KanBehandlingOpprettesManueltRespons {
        val response =
            restClient
                .get()
                .uri(kanBehandlingOpprettesManueltUri(fagsakId, ytelsestype))
                .retrieve()
                .body<Ressurs<KanBehandlingOpprettesManueltRespons>>()!!
        return response.getDataOrThrow()
    }
}
