package no.nav.familie.ef.iverksett.behandlingstatistikk

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.util.opprettBehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID

class BehandlingstatistikkControllerTest : ServerTest() {

    val behandlingstatistikkService: BehandlingstatistikkService = mockk<BehandlingstatistikkService>()

    @MockBean
    lateinit var kafkaProducer: BehandlingstatistikkProducer

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `Sende behandlingsstatistikk skal gi 200 OK`() {

        val behandlingStatistikkDto = opprettBehandlingStatistikkDto(UUID.randomUUID(), Hendelse.MOTTATT, false)
        every { behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto) } just Runs
        val response: ResponseEntity<HttpStatus> =
                restTemplate.exchange(localhostUrl("/api/statistikk/behandlingstatistikk/"), HttpMethod.POST,
                                      HttpEntity(behandlingStatistikkDto, headers))
        Assertions.assertThat(response.statusCode.value()).isEqualTo(200)

    }
}