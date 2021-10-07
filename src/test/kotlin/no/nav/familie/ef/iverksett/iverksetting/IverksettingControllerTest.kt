package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.http.client.MultipartBuilder
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.UUID


class IverksettingControllerTest : ServerTest() {

    private val behandlingId = UUID.randomUUID()

    @Autowired
    lateinit var taskRepository: TaskRepository


    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)

    }

    @Test
    internal fun `starte iverksetting gir 200 OK`() {
        val iverksettJson = opprettIverksettDto(behandlingId = behandlingId)
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .withByteArray("fil", "1", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<Any> = restTemplate.exchange(localhostUrl("/api/iverksett"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
        val tasker = taskRepository.findAll()
        assertThat(tasker.map { it.type }).contains(IverksettMotOppdragTask.TYPE)
        assertThat(tasker.map { it.type }).doesNotContain(JournalførVedtaksbrevTask.TYPE)
    }


    @Test
    internal fun `starte iverksetting for avslag ytelse gir 200 OK`() {
        val iverksettJson = opprettIverksettDto(behandlingId = behandlingId)
        val iverksettJsonMedAvslag =
                iverksettJson.copy(vedtak = iverksettJson.vedtak.copy(tilkjentYtelse = null, resultat = Vedtaksresultat.AVSLÅTT))
        val request = MultipartBuilder()
                .withJson("data", iverksettJsonMedAvslag)
                .withByteArray("fil", "1", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<Any> = restTemplate.exchange(localhostUrl("/api/iverksett"),
                                                                 HttpMethod.POST,
                                                                 HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(200)
        val tasker = taskRepository.findAll()
        assertThat(tasker.map { it.type }).doesNotContain(IverksettMotOppdragTask.TYPE)
        assertThat(tasker.map { it.type }).contains(JournalførVedtaksbrevTask.TYPE)
    }

    @Test
    internal fun `Innvilget vedtak uten tilkjent ytelse gir 400 feil`() {
        val iverksettJson = opprettIverksettDto(behandlingId = behandlingId)
        val iverksettJsonUtenTilkjentYtelse = iverksettJson.copy(vedtak = iverksettJson.vedtak.copy(tilkjentYtelse = null))
        val request = MultipartBuilder()
                .withJson("data", iverksettJsonUtenTilkjentYtelse)
                .withByteArray("fil", "1", byteArrayOf(12))
                .build()

        val respons: ResponseEntity<Ressurs<Nothing>> = restTemplate.exchange(localhostUrl("/api/iverksett"),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(request, headers))
        assertThat(respons.statusCode.value()).isEqualTo(400)
    }

    @Test
    internal fun `mangler brev, forvent 400`() {

        val iverksettJson = opprettIverksettDto(behandlingId = behandlingId)
        val request = MultipartBuilder()
                .withJson("data", iverksettJson)
                .build()

        val respons: ResponseEntity<String> = restTemplate.exchange(localhostUrl("/api/iverksett"),
                                                                    HttpMethod.POST,
                                                                    HttpEntity(request, headers))

        assertThat(respons.statusCode.value()).isEqualTo(400)
    }
}