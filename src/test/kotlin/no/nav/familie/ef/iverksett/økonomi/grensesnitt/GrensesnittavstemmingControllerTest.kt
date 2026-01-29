package no.nav.familie.ef.iverksett.økonomi.grensesnitt

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.exchange

class GrensesnittavstemmingControllerTest : ServerTest() {
    @Autowired
    private lateinit var taskService: TaskService

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `skal opprette grensesnittavstemming for overgangsstønad, barnetilsyn og skolepenger`() {
        StønadType.values().forEachIndexed { index, stønadType ->
            val antall = index + 1
            val grensesnittAvstemmingRequest = GrensesnittavstemmingRequestDto(stønadType = stønadType)

            val responsOk: ResponseEntity<Ressurs<Unit>> = startGrensesnittavstemming(grensesnittAvstemmingRequest)
            assertThrows<HttpClientErrorException.BadRequest> {
                startGrensesnittavstemming(grensesnittAvstemmingRequest)
            }

            assertThat(responsOk.statusCode.value()).isEqualTo(HttpStatus.OK.value())
            assertThat(taskService.finnTasksMedStatus(listOf(Status.UBEHANDLET, Status.KLAR_TIL_PLUKK), GrensesnittavstemmingTask.TYPE)).hasSize(antall)
        }
    }

    private fun startGrensesnittavstemming(grensesnittAvstemmingRequest: GrensesnittavstemmingRequestDto): ResponseEntity<Ressurs<Unit>> =
        restTemplate.exchange(
            localhostUrl("/api/grensesnittavstemming"),
            HttpMethod.POST,
            HttpEntity(grensesnittAvstemmingRequest, headers),
        )
}
