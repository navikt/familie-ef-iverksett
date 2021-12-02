package no.nav.familie.ef.iverksett.behandlingsstatistikk

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettBehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingsstatistikkServiceTest {

    private val behandlingstatistikkRepository = mockk<BehandlingsstatistikkRepository>()
    private val behandlingstatistikkProducer = mockk<BehandlingsstatistikkProducer>(relaxed = true)
    private val featureToggleService = mockk<FeatureToggleService>()
    private val behandlingstatistikkService =
            BehandlingsstatistikkService(behandlingstatistikkProducer, behandlingstatistikkRepository)
    private val captureSlot = slot<Behandlingsstatistikk>()

    @BeforeEach
    fun setUp() {
        every { featureToggleService.isEnabled(any()) } returns true
        every { behandlingstatistikkRepository.insert(capture(captureSlot)) }.answers { captureSlot.captured }
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype MOTTATT, forvent likhet for det som lagres`() {

        val uuid = UUID.randomUUID()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = false)

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingDvh.behandlingId).isEqualTo(behandlingStatistikkDto.behandlingId.toString())
        assertThat(captureSlot.captured.behandlingDvh.personIdent).isEqualTo(behandlingStatistikkDto.personIdent)
        assertThat(captureSlot.captured.behandlingDvh.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.behandlingDvh.saksnummer).isEqualTo(behandlingStatistikkDto.eksternFagsakId)
        assertThat(captureSlot.captured.behandlingDvh.registrertTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.behandlingType).isEqualTo(behandlingStatistikkDto.behandlingstype.name)
        assertThat(captureSlot.captured.behandlingDvh.ansvarligEnhet).isEqualTo(behandlingStatistikkDto.ansvarligEnhet)
        assertThat(captureSlot.captured.behandlingDvh.opprettetEnhet).isEqualTo(behandlingStatistikkDto.opprettetEnhet)
        assertThat(captureSlot.captured.behandlingDvh.sakYtelse).isEqualTo(behandlingStatistikkDto.stønadstype.name)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype PÅBEGYNT, forvent likhet for det som hentes og lagres`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler påbegynt"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.PÅBEGYNT, fortrolig = false)
                .copy(hendelseTidspunkt = endretTid, gjeldendeSaksbehandlerId = saksbehandler)

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingDvh.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.behandlingDvh.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingDvh.behandlingStatus).isEqualTo(Hendelse.PÅBEGYNT.name)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype VEDTATT, forvent likhet for det som hentes og lagres`() {
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

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingDvh.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.vedtakTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.behandlingDvh.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingDvh.behandlingStatus).isEqualTo(Hendelse.VEDTATT.name)
        assertThat(captureSlot.captured.behandlingDvh.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(captureSlot.captured.behandlingDvh.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype BESLUTTET, forvent likhet for det som hentes og lagres`() {

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

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingDvh.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.ansvarligBeslutter)
                .isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.behandlingDvh.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingDvh.behandlingStatus).isEqualTo(Hendelse.BESLUTTET.name)
        assertThat(captureSlot.captured.behandlingDvh.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(captureSlot.captured.behandlingDvh.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype FERDIG, forvent likhet for det som hentes og lagres`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val behandlingStatistikkDto =
                opprettBehandlingsstatistikkDto(uuid, Hendelse.FERDIG, fortrolig = false).copy(hendelseTidspunkt = endretTid)

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingDvh.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingDvh.ferdigBehandletTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingDvh.behandlingStatus).isEqualTo(Hendelse.FERDIG.name)
    }

    @Test
    fun `opprettet behandlingsstatistikk av behandlingDto med fortrolig lik true, forvent -5 konstant på graderte felter`() {

        val uuid = UUID.randomUUID()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = true)

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingDvh.opprettetAv).isEqualTo("-5")
        assertThat(captureSlot.captured.behandlingDvh.opprettetEnhet).isEqualTo("-5")
        assertThat(captureSlot.captured.behandlingDvh.ansvarligEnhet).isEqualTo("-5")

    }

}

