package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.detaljertSimuleringResultat
import no.nav.familie.ef.iverksett.simuleringDto
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.time.LocalDate

class SimuleringControllerTest : ServerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `Hent simulering skal gi 200 OK`() {

        val respons = restTemplate
                .exchange<Ressurs<DetaljertSimuleringResultat>>(localhostUrl("/api/simulering/"),
                                                                HttpMethod.POST,
                                                                HttpEntity(simuleringDto(), headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(respons.body?.data).isEqualTo(detaljertSimuleringResultat())
    }

    @Test
    internal fun `Hent simulering v2 skal gi 200 OK`() {

        val respons = restTemplate
                .exchange<Ressurs<BeriketSimuleringsresultat>>(localhostUrl("/api/simulering/v2/"),
                                                               HttpMethod.POST,
                                                               HttpEntity(simuleringDto(), headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(respons.body?.data).isEqualTo(beriketSimuleringsresultat())
    }

    @Test
    internal fun `simulering av førstegangsbehandling skal gi tomt svar`() {
        val request = simuleringDto(andeler = emptyList(), forrigeBehandlingId = null)
        val respons = restTemplate
                .exchange<Ressurs<BeriketSimuleringsresultat>>(localhostUrl("/api/simulering/v2/"),
                                                               HttpMethod.POST,
                                                               HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        val beriketSimuleringsresultat = lagDefaultBeriketSimuleringsresultat()
        assertThat(respons.body?.data).isEqualTo(beriketSimuleringsresultat)
    }

    @Test
    internal fun `simulering av førstegangsbehandling med kun 0 beløp skal gi tomt svar`() {
        val request = simuleringDto(andeler = listOf(lagAndelTilkjentYtelseDto(beløp = 0)), forrigeBehandlingId = null)
        val respons = restTemplate
                .exchange<Ressurs<BeriketSimuleringsresultat>>(localhostUrl("/api/simulering/v2/"),
                                                               HttpMethod.POST,
                                                               HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        val beriketSimuleringsresultat = lagDefaultBeriketSimuleringsresultat()
        assertThat(respons.body?.data).isEqualTo(beriketSimuleringsresultat)
    }

    private fun lagDefaultBeriketSimuleringsresultat(): BeriketSimuleringsresultat {
        val defaultSimuleringsresultat = DetaljertSimuleringResultat(emptyList())
        val oppsummering = lagSimuleringsoppsummering(defaultSimuleringsresultat, LocalDate.now())
        return BeriketSimuleringsresultat(defaultSimuleringsresultat, oppsummering)
    }


}