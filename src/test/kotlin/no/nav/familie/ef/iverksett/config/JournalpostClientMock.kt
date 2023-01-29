package no.nav.familie.ef.iverksett.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class JournalpostClientMock(@Value("\${FAMILIE_INTEGRASJONER_API_URL}") private val integrasjonUri: URI) {

    private val dokarkivUri: URI = UriComponentsBuilder.fromUri(integrasjonUri).pathSegment("api/arkiv").build().toUri()
    private val pingUri: URI = URI("/ping")
    private val distribuerDokumentUri: URI =
        UriComponentsBuilder.fromUri(integrasjonUri).pathSegment("api/dist/v1").build().toUri()

    private val arkiverDokumentResponse = Ressurs.success(ArkiverDokumentResponse(journalpostId = "1234", ferdigstilt = true))
    private val bestillingId = Ressurs.success("123458888")

    val responses = listOf(
        WireMock.get(WireMock.urlEqualTo(pingUri.path))
            .willReturn(WireMock.aResponse().withStatus(200)),
        WireMock.post(WireMock.urlMatching("${dokarkivUri.path}.*")).atPriority(1)
            .withRequestBody(WireMock.matchingJsonPath("$..id", WireMock.containing("SkalKasteFeil")))
            .willReturn(WireMock.serverError()),
        WireMock.post(WireMock.urlMatching("${dokarkivUri.path}.*")).atPriority(2)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(arkiverDokumentResponse))),
        WireMock.post(WireMock.urlEqualTo(distribuerDokumentUri.path)).atPriority(2)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(bestillingId))),
        WireMock.post(WireMock.urlEqualTo(distribuerDokumentUri.path)).atPriority(1)
            .withRequestBody(WireMock.matchingJsonPath("$..journalpostId", WireMock.containing("SkalFeile")))
            .willReturn(WireMock.serverError()),
    )

    fun journalføringPath(): String = "${dokarkivUri.path}.*"
    fun distribuerPath(): String = "${distribuerDokumentUri.path}.*"

    @Bean("mock-integrasjoner")
    @Profile("mock-integrasjoner")
    fun integrationMockServer(): WireMockServer {
        val mockServer = WireMockServer(9085)
        responses.forEach {
            mockServer.stubFor(it)
        }
        mockServer.start()
        return mockServer
    }
}
