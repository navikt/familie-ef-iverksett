package no.nav.familie.ef.iverksett.behandlingsstatistikk

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettBehandlingsstatistikkDto
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingsstatistikkServiceTest {

    private val behandlingstatistikkProducer = mockk<BehandlingsstatistikkProducer>(relaxed = true)
    private val featureToggleService = mockk<FeatureToggleService>()
    private val behandlingstatistikkService = BehandlingsstatistikkService(behandlingstatistikkProducer)
    private val captureSlot = slot<BehandlingDVH>()

    @BeforeEach
    fun setUp() {
        every { featureToggleService.isEnabled(any()) } returns true
        every { behandlingstatistikkProducer.sendBehandling(capture(captureSlot)) } just runs
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype MOTTATT, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = false)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.behandlingId).isEqualTo(behandlingStatistikkDto.eksternBehandlingId)
        assertThat(captureSlot.captured.personIdent).isEqualTo(behandlingStatistikkDto.personIdent)
        assertThat(captureSlot.captured.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.saksnummer).isEqualTo(behandlingStatistikkDto.eksternFagsakId)
        assertThat(captureSlot.captured.registrertTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingType).isEqualTo(behandlingStatistikkDto.behandlingstype.name)
        assertThat(captureSlot.captured.ansvarligEnhet).isEqualTo(behandlingStatistikkDto.ansvarligEnhet)
        assertThat(captureSlot.captured.opprettetEnhet).isEqualTo(behandlingStatistikkDto.opprettetEnhet)
        assertThat(captureSlot.captured.sakYtelse).isEqualTo(behandlingStatistikkDto.st??nadstype.name)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype P??BEGYNT, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler p??begynt"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.P??BEGYNT, fortrolig = false)
            .copy(hendelseTidspunkt = endretTid, gjeldendeSaksbehandlerId = saksbehandler)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingStatus).isEqualTo(Hendelse.P??BEGYNT.name)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype VEDTATT, mappes korrekt for sending til DVH`() {
        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler som vedtar"
        val behandlingResultat = "Behandlingsresultat fra vedtak"
        val resultatBegrunnelse = "Begrunnelse for vedtak"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.VEDTATT, fortrolig = false)
            .copy(
                hendelseTidspunkt = endretTid,
                gjeldendeSaksbehandlerId = saksbehandler,
                behandlingResultat = behandlingResultat,
                resultatBegrunnelse = resultatBegrunnelse
            )

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.vedtakTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingStatus).isEqualTo(Hendelse.VEDTATT.name)
        assertThat(captureSlot.captured.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(captureSlot.captured.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype BESLUTTET, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val ansvarligBeslutter = "beslutterId"
        val behandlingResultat = "Behandlingsresultat fra beslutter"
        val resultatBegrunnelse = "Begrunnelse for vedtak"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.BESLUTTET, fortrolig = false)
            .copy(
                hendelseTidspunkt = endretTid,
                gjeldendeSaksbehandlerId = ansvarligBeslutter,
                behandlingResultat = behandlingResultat,
                resultatBegrunnelse = resultatBegrunnelse
            )

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.ansvarligBeslutter).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(captureSlot.captured.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.behandlingStatus).isEqualTo(Hendelse.BESLUTTET.name)
        assertThat(captureSlot.captured.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(captureSlot.captured.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `sendBehandlingsstatistikkDto med hendelsestype FERDIG, mappes korrekt for sending til DVH`() {

        val uuid = UUID.randomUUID()
        val endretTid = ZonedDateTime.now()
        val behandlingStatistikkDto =
            opprettBehandlingsstatistikkDto(uuid, Hendelse.FERDIG, fortrolig = false).copy(hendelseTidspunkt = endretTid)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.tekniskTid).isAfter(ZonedDateTime.now().minusSeconds(1))
        assertThat(captureSlot.captured.ferdigBehandletTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(captureSlot.captured.behandlingStatus).isEqualTo(Hendelse.FERDIG.name)
    }

    @Test
    fun `opprettet behandlingsstatistikk av behandlingDto med fortrolig lik true, forvent -5 konstant p?? graderte felter`() {

        val uuid = UUID.randomUUID()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = true)

        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(captureSlot.captured.opprettetAv).isEqualTo("-5")
        assertThat(captureSlot.captured.opprettetEnhet).isEqualTo("-5")
        assertThat(captureSlot.captured.ansvarligEnhet).isEqualTo("-5")
    }
}
