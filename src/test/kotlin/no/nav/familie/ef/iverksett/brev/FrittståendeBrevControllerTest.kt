package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.frittstående.KarakterInnhentingBrevUtil.brevDto
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

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
                localhostUrl("/api/brev/frittstaende/innhenting-karakterutskrift"),
                HttpMethod.POST,
                HttpEntity(brevDto1, headers),
            )

        val respons2: ResponseEntity<Ressurs<Unit>> =
            restTemplate.exchange(
                localhostUrl("/api/brev/frittstaende/innhenting-karakterutskrift"),
                HttpMethod.POST,
                HttpEntity(brevDto2, headers),
            )
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons2.statusCode.value()).isEqualTo(400)
        assertThat(respons2.body?.melding).contains("Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=")
    }
}
