package no.nav.familie.ef.iverksett.behandlingsstatistikk

import io.mockk.CapturingSlot
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.util.opprettBehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID

class BehandlingsstatistikkControllerTest : ServerTest() {


    @Autowired
    private lateinit var kafkaProducerPayloadSlot: CapturingSlot<String>

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `Sende behandlingsstatistikk skal gi 200 OK`() {

        val behandlingId = UUID.randomUUID()
        val mottatt = opprettBehandlingsstatistikkDto(behandlingId, Hendelse.MOTTATT, false)
        Assertions.assertThat(send(mottatt).statusCode.value()).isEqualTo(200)
        Assertions.assertThat(kafkaProducerPayloadSlot.captured).doesNotContain("Z\",")

        val påbegynt = opprettBehandlingsstatistikkDto(behandlingId, Hendelse.PÅBEGYNT, false)
        Assertions.assertThat(send(påbegynt).statusCode.value()).isEqualTo(200)
        Assertions.assertThat(kafkaProducerPayloadSlot.captured).doesNotContain("Z\",")

        val vedtatt = opprettBehandlingsstatistikkDto(behandlingId, Hendelse.VEDTATT, false)
        Assertions.assertThat(send(vedtatt).statusCode.value()).isEqualTo(200)
        Assertions.assertThat(kafkaProducerPayloadSlot.captured).doesNotContain("Z\",")

        val besluttet = opprettBehandlingsstatistikkDto(behandlingId, Hendelse.BESLUTTET, false)
        Assertions.assertThat(send(besluttet).statusCode.value()).isEqualTo(200)
        Assertions.assertThat(kafkaProducerPayloadSlot.captured).doesNotContain("Z\",")

        val ferdig = opprettBehandlingsstatistikkDto(behandlingId, Hendelse.FERDIG, false)
        Assertions.assertThat(send(ferdig).statusCode.value()).isEqualTo(200)
        Assertions.assertThat(kafkaProducerPayloadSlot.captured).doesNotContain("Z\",")


    }

    private fun send(behandlingStatistikkDto: BehandlingsstatistikkDto): ResponseEntity<HttpStatus> =
            restTemplate.exchange(localhostUrl("/api/statistikk/behandlingsstatistikk/"), HttpMethod.POST,
                                  HttpEntity(behandlingStatistikkDto, headers))
}