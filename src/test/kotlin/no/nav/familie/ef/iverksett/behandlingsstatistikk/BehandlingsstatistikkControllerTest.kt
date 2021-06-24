package no.nav.familie.ef.iverksett.behandlingsstatistikk

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.util.opprettBehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFuture
import java.util.UUID

class BehandlingsstatistikkControllerTest : ServerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `Sende behandlingsstatistikk skal gi 200 OK`() {

        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(UUID.randomUUID(), Hendelse.MOTTATT, false)
        val response: ResponseEntity<HttpStatus> =
                restTemplate.exchange(localhostUrl("/api/statistikk/behandlingsstatistikk/"), HttpMethod.POST,
                                      HttpEntity(behandlingStatistikkDto, headers))
        Assertions.assertThat(response.statusCode.value()).isEqualTo(200)

    }
}