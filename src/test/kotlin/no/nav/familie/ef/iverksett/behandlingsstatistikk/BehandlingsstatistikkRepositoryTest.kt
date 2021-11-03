package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingsstatistikkRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var behandlingsstatistikkRepository: BehandlingsstatistikkRepository
    private val behandlingId: UUID = UUID.randomUUID()
    private val behandlingstatistikkPåbegynt = opprettBehandlingstatistikk(behandlingId)

    @BeforeEach
    internal fun beforeEach() {
        behandlingsstatistikkRepository.insert(Behandlingsstatistikk(behandlingId = behandlingId,
                                                                     behandlingDvh = behandlingstatistikkPåbegynt,
                                                                     hendelse = Hendelse.PÅBEGYNT))
    }

    @Test
    fun `hente behandlingstatistikk, forvent likhet for felter som ikke er nullable`() {
        val hentetBehandlingstatistikk =
                behandlingsstatistikkRepository.findByBehandlingIdAndHendelse(behandlingId, Hendelse.PÅBEGYNT)

        assertThat(hentetBehandlingstatistikk.behandlingDvh.behandlingId).isEqualTo(behandlingstatistikkPåbegynt.behandlingId)
        assertThat(hentetBehandlingstatistikk.behandlingDvh.personIdent).isEqualTo(behandlingstatistikkPåbegynt.personIdent)
        assertThat(hentetBehandlingstatistikk.behandlingDvh.registrertTid).isEqualTo(behandlingstatistikkPåbegynt.registrertTid)
        assertThat(hentetBehandlingstatistikk.behandlingDvh.endretTid).isEqualTo(behandlingstatistikkPåbegynt.endretTid)
        assertThat(hentetBehandlingstatistikk.behandlingDvh.tekniskTid).isEqualTo(behandlingstatistikkPåbegynt.tekniskTid)
        assertThat(hentetBehandlingstatistikk.behandlingDvh.mottattTid).isEqualTo(behandlingstatistikkPåbegynt.mottattTid)
        assertThat(hentetBehandlingstatistikk.behandlingDvh.saksbehandler).isEqualTo(behandlingstatistikkPåbegynt.saksbehandler)
    }

    @Test
    fun `lagre og hente behandlingstatistikk med ny hendelse, forvent ingen nullverdi`() {
        behandlingsstatistikkRepository.insert(Behandlingsstatistikk(behandlingId = behandlingId,
                                                                     behandlingDvh = behandlingstatistikkPåbegynt,
                                                                     hendelse = Hendelse.MOTTATT))
        val lagretBehandlingsstatistikk =
                behandlingsstatistikkRepository.findByBehandlingIdAndHendelse(behandlingId, Hendelse.MOTTATT)
        assertThat(lagretBehandlingsstatistikk).isNotNull
    }

    @Test
    fun `hent behandlingstatistikk med ikke-eksisterende id, forvent IllegalStateException`() {
        Assertions.assertThrows(EmptyResultDataAccessException::class.java) {
            behandlingsstatistikkRepository.findByBehandlingIdAndHendelse(UUID.randomUUID(), Hendelse.PÅBEGYNT)
        }

    }

    @Test
    fun `hent behandlingstatistikk med ikke-eksisterende hendelse, forvent IllegalStateException`() {
        Assertions.assertThrows(EmptyResultDataAccessException::class.java) {
            behandlingsstatistikkRepository.findByBehandlingIdAndHendelse(UUID.randomUUID(), Hendelse.FERDIG)
        }
    }

    private fun opprettBehandlingstatistikk(behandlingId: UUID): BehandlingDVH {
        return BehandlingDVH(behandlingId = behandlingId.toString(),
                             personIdent = "persinIdent",
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
                             avsender = "NAV Enslig forelder",
                             totrinnsbehandling = true,
                             sakUtland = "Nasjonal")
    }
}