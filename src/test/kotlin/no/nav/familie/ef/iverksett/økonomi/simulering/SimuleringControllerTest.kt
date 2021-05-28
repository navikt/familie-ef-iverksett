package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.detaljertSimuleringResultat
import no.nav.familie.ef.iverksett.simuleringDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

class SimuleringControllerTest : ServerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `Hent simulering skal gi 200 OK`() {

        val respons = hentSimulering(simuleringDto())
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(respons.body?.data).isEqualTo(detaljertSimuleringResultat())
    }

    private fun hentSimulering(simuleringDto: SimuleringDto): ResponseEntity<Ressurs<DetaljertSimuleringResultat>> {
        return restTemplate.exchange(localhostUrl("/api/simulering/"),
            HttpMethod.POST,
            HttpEntity(simuleringDto, headers)
        )
    }
}