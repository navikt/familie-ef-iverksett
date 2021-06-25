package no.nav.familie.ef.iverksett.behandlingsstatistikk

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.util.opprettBehandlingDVH
import no.nav.familie.ef.iverksett.util.opprettBehandlingsstatistikkDto
import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.ef.sak.featuretoggle.FeatureToggleService
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingsstatistikkServiceTest {

    val behandlingstatistikkRepository = mockk<BehandlingsstatistikkRepository>()
    val behandlingstatistikkProducer = mockk<BehandlingsstatistikkProducer>()
    val featureToggleService = mockk<FeatureToggleService>()
    val behandlingstatistikkService = BehandlingsstatistikkService(behandlingstatistikkProducer, behandlingstatistikkRepository, featureToggleService)

    @BeforeEach
    fun setUp() {
        every { behandlingstatistikkProducer.sendBehandling(any()) } just Runs
        every { featureToggleService.isEnabled(any()) } returns true
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype MOTTATT, forvent likhet for det som lagres`() {

        val uuid = UUID.randomUUID()
        val behandlingDVHSlot = slot<BehandlingDVH>()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = false)

        every { behandlingstatistikkRepository.lagre(any(), capture(behandlingDVHSlot), any()) } just Runs

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(behandlingDVHSlot.captured.behandlingId).isEqualTo(behandlingStatistikkDto.behandlingId.toString())
        assertThat(behandlingDVHSlot.captured.personIdent).isEqualTo(behandlingStatistikkDto.personIdent)
        assertThat(behandlingDVHSlot.captured.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(behandlingDVHSlot.captured.saksnummer).isEqualTo(behandlingStatistikkDto.eksternFagsakId)
        assertThat(behandlingDVHSlot.captured.registrertTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.behandlingType).isEqualTo(behandlingStatistikkDto.behandlingstype.name)
        assertThat(behandlingDVHSlot.captured.ansvarligEnhet).isEqualTo(behandlingStatistikkDto.ansvarligEnhet)
        assertThat(behandlingDVHSlot.captured.opprettetEnhet).isEqualTo(behandlingStatistikkDto.opprettetEnhet)
        assertThat(behandlingDVHSlot.captured.sakYtelse).isEqualTo(behandlingStatistikkDto.stønadstype.tilKlassifisering())
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype PÅBEGYNT, forvent likhet for det som hentes og lagres`() {

        val uuid = UUID.randomUUID()
        val behandlingDVHSlot = slot<BehandlingDVH>()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler påbegynt"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.PÅBEGYNT, fortrolig = false).copy(
                hendelseTidspunkt = endretTid,
                gjeldendeSaksbehandlerId = saksbehandler)
        val behandlingDVH = opprettBehandlingDVH(uuid, Hendelse.MOTTATT)

        every { behandlingstatistikkRepository.lagre(any(), capture(behandlingDVHSlot), any()) } just Runs
        every { behandlingstatistikkRepository.hent(any(), any()) } returns behandlingDVH

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(behandlingDVHSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(behandlingDVHSlot.captured.tekniskTid).isNotEqualTo(behandlingDVH.tekniskTid)
        assertThat(behandlingDVHSlot.captured.behandlingStatus).isEqualTo(Hendelse.PÅBEGYNT.name)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype VEDTATT, forvent likhet for det som hentes og lagres`() {
        val uuid = UUID.randomUUID()
        val behandlingDVHSlot = slot<BehandlingDVH>()
        val endretTid = ZonedDateTime.now()
        val saksbehandler = "Saksbehandler som vedtar"
        val behandlingResultat = "Behandlingsresultat fra vedtak"
        val resultatBegrunnelse = "Begrunnelse for vedtak"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.VEDTATT, fortrolig = false).copy(
                hendelseTidspunkt = endretTid,
                gjeldendeSaksbehandlerId = saksbehandler,
                behandlingResultat = behandlingResultat,
                resultatBegrunnelse = resultatBegrunnelse
        )
        val behandlingDVH = opprettBehandlingDVH(uuid, Hendelse.PÅBEGYNT)

        every { behandlingstatistikkRepository.lagre(any(), capture(behandlingDVHSlot), any()) } just Runs
        every { behandlingstatistikkRepository.hent(any(), any()) } returns behandlingDVH

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(behandlingDVHSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.vedtakTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.saksbehandler).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(behandlingDVHSlot.captured.tekniskTid).isNotEqualTo(behandlingDVH.tekniskTid)
        assertThat(behandlingDVHSlot.captured.behandlingStatus).isEqualTo(Hendelse.VEDTATT.name)
        assertThat(behandlingDVHSlot.captured.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(behandlingDVHSlot.captured.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype BESLUTTET, forvent likhet for det som hentes og lagres`() {

        val uuid = UUID.randomUUID()
        val behandlingDVHSlot = slot<BehandlingDVH>()
        val endretTid = ZonedDateTime.now()
        val ansvarligBeslutter = "Saksbehandler som beslutter"
        val behandlingResultat = "Behandlingsresultat fra beslutter"
        val resultatBegrunnelse = "Begrunnelse for vedtak"
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.BESLUTTET, fortrolig = false).copy(
                hendelseTidspunkt = endretTid,
                gjeldendeSaksbehandlerId = ansvarligBeslutter,
                behandlingResultat = behandlingResultat,
                resultatBegrunnelse = resultatBegrunnelse
        )
        val behandlingDVH = opprettBehandlingDVH(uuid, Hendelse.VEDTATT)

        every { behandlingstatistikkRepository.lagre(any(), capture(behandlingDVHSlot), any()) } just Runs
        every { behandlingstatistikkRepository.hent(any(), any()) } returns behandlingDVH

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(behandlingDVHSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.ansvarligBeslutter).isEqualTo(behandlingStatistikkDto.gjeldendeSaksbehandlerId)
        assertThat(behandlingDVHSlot.captured.tekniskTid).isNotEqualTo(behandlingDVH.tekniskTid)
        assertThat(behandlingDVHSlot.captured.behandlingStatus).isEqualTo(Hendelse.BESLUTTET.name)
        assertThat(behandlingDVHSlot.captured.behandlingResultat).isEqualTo(behandlingStatistikkDto.behandlingResultat)
        assertThat(behandlingDVHSlot.captured.resultatBegrunnelse).isEqualTo(behandlingStatistikkDto.resultatBegrunnelse)
    }

    @Test
    fun `lagre BehandlingsstatistikkDto med hendelsestype FERDIG, forvent likhet for det som hentes og lagres`() {

        val uuid = UUID.randomUUID()
        val behandlingDVHSlot = slot<BehandlingDVH>()
        val endretTid = ZonedDateTime.now()
        val behandlingStatistikkDto =
                opprettBehandlingsstatistikkDto(uuid, Hendelse.FERDIG, fortrolig = false).copy(hendelseTidspunkt = endretTid)
        val behandlingDVH = opprettBehandlingDVH(uuid, Hendelse.VEDTATT)

        every { behandlingstatistikkRepository.lagre(any(), capture(behandlingDVHSlot), any()) } just Runs
        every { behandlingstatistikkRepository.hent(any(), any()) } returns behandlingDVH

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(behandlingDVHSlot.captured.endretTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.tekniskTid).isNotEqualTo(behandlingDVH.tekniskTid)
        assertThat(behandlingDVHSlot.captured.ferdigBehandletTid).isEqualTo(behandlingStatistikkDto.hendelseTidspunkt)
        assertThat(behandlingDVHSlot.captured.behandlingStatus).isEqualTo(Hendelse.FERDIG.name)
    }

    @Test
    fun `opprettet behandlingDVH av behandlingDto med fortrolig lik true, forvent -5 konstant på graderte felter`() {

        val uuid = UUID.randomUUID()
        val behandlingDVHSlot = slot<BehandlingDVH>()
        val behandlingStatistikkDto = opprettBehandlingsstatistikkDto(uuid, Hendelse.MOTTATT, fortrolig = true)

        every { behandlingstatistikkRepository.lagre(any(), capture(behandlingDVHSlot), any()) } just Runs

        behandlingstatistikkService.lagreBehandlingstatistikk(behandlingStatistikkDto)

        assertThat(behandlingDVHSlot.captured.opprettetAv).isEqualTo("-5")
        assertThat(behandlingDVHSlot.captured.opprettetEnhet).isEqualTo("-5")
        assertThat(behandlingDVHSlot.captured.ansvarligEnhet).isEqualTo("-5")

    }

}

