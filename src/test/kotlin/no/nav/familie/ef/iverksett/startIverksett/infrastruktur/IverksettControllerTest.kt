package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.BehandlingResultat
import no.nav.familie.ef.iverksett.domene.BehandlingType
import no.nav.familie.ef.iverksett.domene.BehandlingÅrsak
import no.nav.familie.ef.iverksett.domene.OpphørÅrsak
import no.nav.familie.ef.iverksett.domene.Vedtak
import no.nav.familie.ef.iverksett.infrastruktur.json.AktivitetskravJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevdataJson
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonJson
import no.nav.familie.http.client.MultipartBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*


class IverksettControllerTest : ServerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)

    }

    @Test
    internal fun `starte iverksetting gir 200 OK`() {
        val iverksettJson = opprettIverksettJson()
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .withByteArray("fil", "1", byteArrayOf(12))
                .withByteArray("fil", "2", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<Any> = restTemplate.exchange(localhostUrl("/api/iverksett/"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
    }

    @Test
    internal fun `mangler brev`() {
        val iverksettJson = opprettIverksettJson()
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .build()

        val respons: ResponseEntity<Any> = restTemplate.exchange(localhostUrl("/api/iverksett/"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))

        assertThat(respons.statusCode.value()).isEqualTo(400)
    }

    @Test
    internal fun `feil filename på brev`() {
        val iverksettJson = opprettIverksettJson()
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .withByteArray("fil", "11", byteArrayOf(12))
                .withByteArray("fil", "22", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<Any> = restTemplate.exchange(localhostUrl("/api/iverksett/"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))

        assertThat(respons.statusCode.value()).isEqualTo(500)
    }

    private fun opprettIverksettJson(): IverksettJson {
        return IverksettJson(
                brev = listOf(BrevJson(
                        "1", BrevdataJson("mottaker", "saksbehandler")),
                              BrevJson("2", BrevdataJson("mottaker", "saksbehandler"))),
                vedtak = Vedtak.INNVILGET,
                forrigeTilkjentYtelse = emptyList(),
                tilkjentYtelse = emptyList(),
                fagsakId = "1",
                saksnummer = "1",
                behandlingId = UUID.randomUUID().toString(),
                eksternId = 1L,
                relatertBehandlingId = "2",
                kode6eller7 = false,
                tidspunktVedtak = OffsetDateTime.now(),
                vilkårsvurderinger = emptyList(),
                person = PersonJson(personIdent = "12345678910", aktorId = null),
                barn = emptyList(),
                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                behandlingResultat = BehandlingResultat.FERDIGSTILT,
                opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                aktivitetskrav = AktivitetskravJson(LocalDate.now(), false),
                funksjonellId = "0",
                behandlingÅrsak = BehandlingÅrsak.SØKNAD
        )
    }
}