package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingstatistikkRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var behandlingstatistikkRepository: BehandlingstatistikkRepository
    val behandlingId = UUID.randomUUID()
    val behandlingstatistikkPåbegynt = opprettBehandlingstatistikk(behandlingId)

    @BeforeEach
    fun beforeEach() {
        behandlingstatistikkRepository.lagre(behandlingId, behandlingstatistikkPåbegynt, Hendelse.PÅBEGYNT)
    }

    @Test
    fun `hente behandlingstatistikk, forvent ingen unntak`() {
        val hentetBehandlingstatistikk = behandlingstatistikkRepository.hent(behandlingId, Hendelse.PÅBEGYNT)

        assertThat(hentetBehandlingstatistikk?.behandlingId).isEqualTo(behandlingstatistikkPåbegynt.behandlingId)
        assertThat(hentetBehandlingstatistikk?.aktorId).isEqualTo(behandlingstatistikkPåbegynt.aktorId)
        assertThat(hentetBehandlingstatistikk?.registrertTid).isEqualTo(behandlingstatistikkPåbegynt.registrertTid)
        assertThat(hentetBehandlingstatistikk?.endretTid).isEqualTo(behandlingstatistikkPåbegynt.endretTid)
        assertThat(hentetBehandlingstatistikk?.tekniskTid).isEqualTo(behandlingstatistikkPåbegynt.tekniskTid)
        assertThat(hentetBehandlingstatistikk?.mottattTid).isEqualTo(behandlingstatistikkPåbegynt.mottattTid)
        assertThat(hentetBehandlingstatistikk?.saksbehandler).isEqualTo(behandlingstatistikkPåbegynt.saksbehandler)
    }

    @Test
    fun `lagre og hente behandlingstatistikk med ny hendelse, forvent ingen unntak`() {
        val behandlingstatistikkMottat = opprettBehandlingstatistikk(behandlingId)
        behandlingstatistikkRepository.lagre(behandlingId, behandlingstatistikkMottat, Hendelse.MOTTATT)
        val hentetHendelseMottatt = behandlingstatistikkRepository.hent(behandlingId, Hendelse.MOTTATT)

        assertThat(hentetHendelseMottatt?.behandlingId).isEqualTo(behandlingstatistikkMottat.behandlingId)
        assertThat(hentetHendelseMottatt?.aktorId).isEqualTo(behandlingstatistikkMottat.aktorId)
        assertThat(hentetHendelseMottatt?.registrertTid).isEqualTo(behandlingstatistikkMottat.registrertTid)
        assertThat(hentetHendelseMottatt?.endretTid).isEqualTo(behandlingstatistikkMottat.endretTid)
        assertThat(hentetHendelseMottatt?.tekniskTid).isEqualTo(behandlingstatistikkMottat.tekniskTid)
        assertThat(hentetHendelseMottatt?.mottattTid).isEqualTo(behandlingstatistikkMottat.mottattTid)
    }

    @Test
    fun `hent behandlingstatistikk med ikke-eksisterende id, forvent nullverdi i retur og ingen unntak`() {
        Assertions.assertThrows(NullPointerException::class.java) {
            behandlingstatistikkRepository.hent(UUID.randomUUID(), Hendelse.PÅBEGYNT)
        }

    }

    @Test
    fun `hent behandlingstatistikk med ikke-eksisterende hendelse, forvent nullverdi i retur og ingen unntak`() {
        Assertions.assertThrows(NullPointerException::class.java) {
            behandlingstatistikkRepository.hent(UUID.randomUUID(), Hendelse.FERDIG)
        }
    }

    private fun opprettBehandlingstatistikk(behandlingId: UUID): BehandlingDVH {
        return BehandlingDVH(behandlingId = behandlingId.toString(),
                             aktorId = "persinIdent",
                             saksbehandler = "gjeldendeSaksbehandlerId",
                             registrertTid = ZonedDateTime.now(),
                             endretTid = ZonedDateTime.now(),
                             tekniskTid = ZonedDateTime.now(),
                             sakYtelse = "EFOG",
                             behandlingType = "Førstegangsbehandling",
                             behandlingStatus = "MOTTATT",
                             opprettetAv = "gjeldendeSaksbehandlerId",
                             opprettetEnhet = "",
                             ansvarligEnhet = "",
                             saksnummer = "saksnummer",
                             mottattTid = ZonedDateTime.now(),
                             behandlingMetode = "MANUELL",
                             avsender = "NAV Enslig forelder")
    }
}