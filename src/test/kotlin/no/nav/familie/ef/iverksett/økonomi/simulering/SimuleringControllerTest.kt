package no.nav.familie.ef.iverksett.økonomi.simulering

import io.mockk.clearMocks
import io.mockk.verify
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.detaljertSimuleringResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.simuleringDto
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelseMedMetadata
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.time.LocalDate
import java.util.UUID

class SimuleringControllerTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepository: TilstandRepository

    @Autowired
    private lateinit var oppdragClient: OppdragClient

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @AfterEach
    internal fun tearDown() {
        clearMocks(oppdragClient, answers = false)
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
        verify(exactly = 1) { oppdragClient.hentSimuleringsresultat(any()) }
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
        verify(exactly = 1) { oppdragClient.hentSimuleringsresultat(any()) }
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
        assertThat(respons.body?.data).isEqualTo(lagSimuleringsresultatMedTomListe())
        verify(exactly = 0) { oppdragClient.hentSimuleringsresultat(any()) }
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
        assertThat(respons.body?.data).isEqualTo(lagSimuleringsresultatMedTomListe())
        verify(exactly = 0) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    @Test
    internal fun `simulering av revurdering hvor førstegangsbehandling hadde kun 0 beløp skal gi svar`() {
        val behandlingId = UUID.randomUUID()
        lagFørstegangsbehandlingUtenBeløp(behandlingId)

        val revurdering =
                simuleringDto(andeler = listOf(lagAndelTilkjentYtelseDto(beløp = 1000)), forrigeBehandlingId = behandlingId)

        val response = restTemplate.exchange<Ressurs<BeriketSimuleringsresultat>>(localhostUrl("/api/simulering/v2/"),
                                                                                  HttpMethod.POST,
                                                                                  HttpEntity(revurdering, headers))

        assertThat(response.body?.data?.detaljer?.simuleringMottaker).isNotEmpty
        verify(exactly = 1) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    @Test
    internal fun `simulering av revurdering med 0 i beløp hvor førstegangsbehandling også hadde kun 0 beløp skal gi svar`() {
        val behandlingId = UUID.randomUUID()
        lagFørstegangsbehandlingUtenBeløp(behandlingId)

        val revurdering =
                simuleringDto(andeler = listOf(lagAndelTilkjentYtelseDto(beløp = 0)), forrigeBehandlingId = behandlingId)

        val respons = restTemplate.exchange<Ressurs<BeriketSimuleringsresultat>>(localhostUrl("/api/simulering/v2/"),
                                                                                 HttpMethod.POST,
                                                                                 HttpEntity(revurdering, headers))

        assertThat(respons.body?.data).isEqualTo(lagSimuleringsresultatMedTomListe())
        verify(exactly = 0) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    private fun lagFørstegangsbehandlingUtenBeløp(behandlingId: UUID) {
        val andelTilkjentYtelse =
                lagAndelTilkjentYtelse(0, fraOgMed = LocalDate.of(2021, 1, 1), tilOgMed = LocalDate.of(2021, 1, 31))
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingId, andeler = listOf(andelTilkjentYtelse))
        val tilkjentYtelseMedUtbetalingsoppdrag =
                lagTilkjentYtelseMedUtbetalingsoppdrag(opprettTilkjentYtelseMedMetadata(behandlingId, 1L, tilkjentYtelse))

        tilstandRepository.opprettTomtResultat(behandlingId)
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseMedUtbetalingsoppdrag)
    }

    private fun lagSimuleringsresultatMedTomListe(): BeriketSimuleringsresultat {
        val defaultSimuleringsresultat = DetaljertSimuleringResultat(emptyList())
        val oppsummering = lagSimuleringsoppsummering(defaultSimuleringsresultat, LocalDate.now())
        return BeriketSimuleringsresultat(defaultSimuleringsresultat, oppsummering)
    }


}