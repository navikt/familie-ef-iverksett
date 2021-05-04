package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksettJson
import no.nav.familie.http.client.MultipartBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.*


class IverksettControllerTest : ServerTest() {

    private val behandlingId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)

    }

    @Test
    internal fun `starte iverksetting gir 200 OK`() {
        val listMedBrev = listOf(opprettBrev("1"), opprettBrev("2"))

        val iverksettJson = opprettIverksettJson(behandlingId = behandlingId.toString(), listMedBrev)
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .withByteArray("fil", "1", byteArrayOf(12))
                .withByteArray("fil", "2", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<Any> = restTemplate.exchange(localhostUrl("/api/iverksett/"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
    }

    @Test
    internal fun `mangler brev, forvent 500`() {
        val listMedBrev = listOf(opprettBrev("1"), opprettBrev("2"))

        val iverksettJson = opprettIverksettJson(behandlingId = behandlingId.toString(), listMedBrev)
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .build()

        val respons: ResponseEntity<String> = restTemplate.exchange(localhostUrl("/api/iverksett/"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))

        assertThat(respons.statusCode.value()).isEqualTo(500)
    }

    @Test
    internal fun `feil filename på brev, forvent 500`() {
        val listMedBrev = listOf(opprettBrev("1"), opprettBrev("2"))
        val iverksettJson = opprettIverksettJson(behandlingId = behandlingId.toString(), listMedBrev)
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .withByteArray("fil", "11", byteArrayOf(12))
                .withByteArray("fil", "22", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<String> = restTemplate.exchange(localhostUrl("/api/iverksett/"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))

        assertThat(respons.statusCode.value()).isEqualTo(500)
    }
}