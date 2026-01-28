package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.AktivitetspliktInnhentingBrevUtil.brevDto
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.exchange

class FrittståendeBrevControllerTest : ServerTest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `Skal ikke kunne opprette to task for samme fagsak-brev-skoleår`() {
        val eksternFagsakId = 123L

        val brevDto1 = brevDto(eksternFagsakId, 1L)
        val brevDto2 = brevDto(eksternFagsakId, 2L)

        val respons: ResponseEntity<Ressurs<Unit>> =
            restTemplate.exchange(
                localhostUrl("/api/brev/frittstaende/innhenting-aktivitetsplikt"),
                HttpMethod.POST,
                HttpEntity(brevDto1, headers),
            )

        val exception = assertThrows<HttpClientErrorException.BadRequest> {
            restTemplate.exchange<ResponseEntity<Ressurs<Unit>>>(
                localhostUrl("/api/brev/frittstaende/innhenting-aktivitetsplikt"),
                HttpMethod.POST,
                HttpEntity(brevDto2, headers),
            )
        }
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(exception.message).contains("Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=")
    }
}
