package no.nav.familie.ef.iverksett.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okForContentType
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandling
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingsstatus
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.FinnesBehandlingResponse
import no.nav.familie.kontrakter.felles.tilbakekreving.KanBehandlingOpprettesManueltRespons
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@Component
class TilbakekrevingClientMock {
    private val pingUri: URI = URI("/ping")

    private val pdf = "Dette er en PDF!"
    private val ok = objectMapper.writeValueAsString(Ressurs.success("ok"))
    private val behandlingId = objectMapper.writeValueAsString(Ressurs.success(UUID.randomUUID().toString()))
    private val finnesBehandlingResponse = objectMapper.writeValueAsString(Ressurs.success(FinnesBehandlingResponse(true)))
    private val behandlinger =
        objectMapper.writeValueAsString(
            Ressurs.success(
                listOf(
                    Behandling(
                        behandlingId = UUID.randomUUID(),
                        opprettetTidspunkt = LocalDateTime.now(),
                        aktiv = true,
                        type = Behandlingstype.TILBAKEKREVING,
                        status = Behandlingsstatus.UTREDES,
                        årsak = null,
                        vedtaksdato = LocalDateTime.now(),
                        resultat = null,
                    ),
                ),
            ),
        )
    private val kanOpprettesManuelt =
        objectMapper.writeValueAsString(Ressurs.success(KanBehandlingOpprettesManueltRespons(true, "Bob")))

    val responses =
        listOf(
            get(pingUri.path)
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson<Any>()),
            post(urlMatching("/api/dokument/forhandsvis-varselbrev"))
                .willReturn(okForContentType("application/pdf", pdf)),
            post(urlMatching("/api/behandling/v1"))
                .willReturn(okJson(behandlingId)),
            post(urlMatching("/api/behandling/manuelt/task/v1"))
                .willReturn(okJson(ok)),
            get(urlMatching("/api/fagsystem/${Fagsystem.EF}/fagsak/.+/finnesApenBehandling/v1"))
                .willReturn(okJson(finnesBehandlingResponse)),
            get(urlMatching("/api/fagsystem/${Fagsystem.EF}/fagsak/.+/behandlinger/v1"))
                .willReturn(okJson(behandlinger)),
            get(urlMatching("/api/ytelsestype/.+/fagsak/.+/kanBehandlingOpprettesManuelt/v1"))
                .willReturn(okJson(kanOpprettesManuelt)),
        )

    @Bean("mock-tilbakekreving")
    @Profile("mock-tilbakekreving")
    fun integrationMockServer(): WireMockServer {
        val mockServer = WireMockServer(8030)
        responses.forEach {
            mockServer.stubFor(it)
        }
        mockServer.start()
        return mockServer
    }
}
