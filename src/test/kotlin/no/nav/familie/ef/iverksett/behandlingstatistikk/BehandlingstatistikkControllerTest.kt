package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.ZonedDateTime
import java.util.UUID

class BehandlingstatistikkControllerTest : ServerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `Sende behandlingsstatistikk skal gi 200 OK`() {
        val behandlingStatistikkDto = BehandlingStatistikkDto(UUID.randomUUID(), "aktor", "saksbehandler", "saksnummer",
                                                              ZonedDateTime.now(), Hendelse.PÅBEGYNT, "", "")
        val response: ResponseEntity<HttpStatus> =
                restTemplate.exchange(localhostUrl("/api/statistikk/behandlingstatistikk/"), HttpMethod.POST,
                                      HttpEntity(behandlingStatistikkDto, headers))
        Assertions.assertThat(response.statusCode.value()).isEqualTo(200)

    }
}