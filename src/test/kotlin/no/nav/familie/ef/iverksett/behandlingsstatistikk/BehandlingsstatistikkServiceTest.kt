package no.nav.familie.ef.iverksett.behandlingsstatistikk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettBehandlingsstatistikkDto
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingsstatistikkServiceTest {

    private val kafkaOperations = mockk<KafkaOperations<String, String>>(relaxed = true)
    private val featureToggleService = mockk<FeatureToggleService>()
    private val behandlingstatistikkService = BehandlingsstatistikkService(kafkaOperations, "topic")
    private val captureSlot = slot<String>()

    @BeforeEach
    fun setUp() {
        every { featureToggleService.isEnabled(any()) } returns true
        val future: SettableListenableFuture<SendResult<String, String>> = SettableListenableFuture()
        every { kafkaOperations.send(any(), any(), capture(captureSlot)) } returns future
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype MOTTATT, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = false)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)
        val capturedBehandlingsstatistikk = objectMapper.readValue<BehandlingDVH>(captureSlot.captured)
        assertThat(capturedBehandlingsstatistikk.behandlingId).isEqualTo(behandlingStatistikkDto.eksternBehandlingId)
        assertThat(capturedBehandlingsstatistikk.personIdent).isEqualTo(behandlingStatistikkDto.personIdent)
        assertThat(capturedBehandlingsstatistikk.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(capturedBehandlingsstatistikk.saksnummer).isEqualTo(behandlingStatistikkDto.eksternFagsakId)
        assertThat(capturedBehandlingsstatistikk.registrertTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.behandlingType).isEqualTo(behandlingStatistikkDto.behandlingstype.name)
        assertThat(capturedBehandlingsstatistikk.ansvarligEnhet).isEqualTo(behandlingStatistikkDto.ansvarligEnhet)
        assertThat(capturedBehandlingsstatistikk.opprettetEnhet).isEqualTo(behandlingStatistikkDto.opprettetEnhet)
        assertThat(capturedBehandlingsstatistikk.sakYtelse).isEqualTo(behandlingStatistikkDto.stønadstype.name)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype PÅBEGYNT, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler påbegynt"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.PÅBEGYNT, fortrolig = false)
                .copy(hendelseTidspunkt = endretTid, gjeldendeSaksbehandlerId = saksbehandler)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        val capturedBehandlingsstatistikk = objectMapper.readValue<BehandlingDVH>(captureSlot.captured)
        assertThat(capturedBehandlingsstatistikk.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(capturedBehandlingsstatistikk.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(capturedBehandlingsstatistikk.behandlingStatus).isEqualTo(Hendelse.PÅBEGYNT.name)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype VEDTATT, mappes korrekt for sending til DVH`() {
        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler som vedtar"
        val behandlingResultat = "Behandlingsresultat fra vedtak"
        val resultatBegrunnelse = "Begrunnelse for vedtak"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.VEDTATT, fortrolig = false)
                .copy(hendelseTidspunkt = endretTid,
                      gjeldendeSaksbehandlerId = saksbehandler,
                      behandlingResultat = behandlingResultat,
                      resultatBegrunnelse = resultatBegrunnelse)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        val capturedBehandlingsstatistikk = objectMapper.readValue<BehandlingDVH>(captureSlot.captured)
        assertThat(capturedBehandlingsstatistikk.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.vedtakTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(capturedBehandlingsstatistikk.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(capturedBehandlingsstatistikk.behandlingStatus).isEqualTo(Hendelse.VEDTATT.name)
        assertThat(capturedBehandlingsstatistikk.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(capturedBehandlingsstatistikk.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype BESLUTTET, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val ansvarligBeslutter = "Saksbehandler som beslutter"
        val behandlingResultat = "Behandlingsresultat fra beslutter"
        val resultatBegrunnelse = "Begrunnelse for vedtak"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.BESLUTTET, fortrolig = false)
                .copy(hendelseTidspunkt = endretTid,
                      gjeldendeSaksbehandlerId = ansvarligBeslutter,
                      behandlingResultat = behandlingResultat,
                      resultatBegrunnelse = resultatBegrunnelse)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        val capturedBehandlingsstatistikk = objectMapper.readValue<BehandlingDVH>(captureSlot.captured)
        assertThat(capturedBehandlingsstatistikk.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.ansvarligBeslutter).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(capturedBehandlingsstatistikk.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(capturedBehandlingsstatistikk.behandlingStatus).isEqualTo(Hendelse.BESLUTTET.name)
        assertThat(capturedBehandlingsstatistikk.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(capturedBehandlingsstatistikk.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype FERDIG, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val behandlingStatistikkDto =
                opprettBehandlingsstatistikkDto(uuid, Hendelse.FERDIG, fortrolig = false).copy(hendelseTidspunkt = endretTid)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        val capturedBehandlingsstatistikk = objectMapper.readValue<BehandlingDVH>(captureSlot.captured)
        assertThat(capturedBehandlingsstatistikk.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(capturedBehandlingsstatistikk.ferdigBehandletTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(capturedBehandlingsstatistikk.behandlingStatus).isEqualTo(Hendelse.FERDIG.name)
    }

    @Test
    fun `opprettet behandlingsstatistikk av behandlingDto med fortrolig lik true, forvent -5 konstant på graderte felter`() {

        val uuid = UUID.randomUUID()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = true)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        val capturedBehandlingsstatistikk = objectMapper.readValue<BehandlingDVH>(captureSlot.captured)
        assertThat(capturedBehandlingsstatistikk.opprettetAv).isEqualTo("-5")
        assertThat(capturedBehandlingsstatistikk.opprettetEnhet).isEqualTo("-5")
        assertThat(capturedBehandlingsstatistikk.ansvarligEnhet).isEqualTo("-5")
    }
}