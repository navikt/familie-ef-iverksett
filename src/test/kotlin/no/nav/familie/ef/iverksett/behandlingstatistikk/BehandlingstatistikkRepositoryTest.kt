package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import java.time.ZonedDateTime
import java.util.UUID

internal class BehandlingstatistikkRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var behandlingstatistikkRepository: BehandlingstatistikkRepository
    val behandlingId = UUID.randomUUID()
    val behandlingstatistikkPåbegynt = opprettBehandlingstatistikk(behandlingId)

    @BeforeEach
    internal fun beforeEach() {
        behandlingstatistikkRepository.lagre(behandlingId, behandlingstatistikkPåbegynt, Hendelse.PÅBEGYNT)
    }

    @Test
    fun `hente behandlingstatistikk, forvent likhet for felter som ikke er nullable`() {
        val hentetBehandlingstatistikk = behandlingstatistikkRepository.hent(behandlingId, Hendelse.PÅBEGYNT)

        assertThat(hentetBehandlingstatistikk.behandlingId).isEqualTo(behandlingstatistikkPåbegynt.behandlingId)
        assertThat(hentetBehandlingstatistikk.personIdent).isEqualTo(behandlingstatistikkPåbegynt.personIdent)
        assertThat(hentetBehandlingstatistikk.registrertTid).isEqualTo(behandlingstatistikkPåbegynt.registrertTid)
        assertThat(hentetBehandlingstatistikk.endretTid).isEqualTo(behandlingstatistikkPåbegynt.endretTid)
        assertThat(hentetBehandlingstatistikk.tekniskTid).isEqualTo(behandlingstatistikkPåbegynt.tekniskTid)
        assertThat(hentetBehandlingstatistikk.mottattTid).isEqualTo(behandlingstatistikkPåbegynt.mottattTid)
        assertThat(hentetBehandlingstatistikk.saksbehandler).isEqualTo(behandlingstatistikkPåbegynt.saksbehandler)
    }

    @Test
    fun `lagre og hente behandlingstatistikk med ny hendelse, forvent ingen unntak`() {
        val behandlingstatistikkMottat = opprettBehandlingstatistikk(behandlingId)
        behandlingstatistikkRepository.lagre(behandlingId, behandlingstatistikkMottat, Hendelse.MOTTATT)
        val behandlingDVH = behandlingstatistikkRepository.hent(behandlingId, Hendelse.MOTTATT)
        assertThat(behandlingDVH).isNotNull
    }

    @Test
    fun `hent behandlingstatistikk med ikke-eksisterende id, forvent IllegalStateException`() {
        Assertions.assertThrows(IllegalStateException::class.java) {
            behandlingstatistikkRepository.hent(UUID.randomUUID(), Hendelse.PÅBEGYNT)
        }

    }

    @Test
    fun `hent behandlingstatistikk med ikke-eksisterende hendelse, forvent IllegalStateException`() {
        Assertions.assertThrows(IllegalStateException::class.java) {
            behandlingstatistikkRepository.hent(UUID.randomUUID(), Hendelse.FERDIG)
        }
    }

    @Test
    fun `lagre samme hendelse to ganger, forvent DuplicateKeyException`() {
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            val behandlingDVH = opprettBehandlingstatistikk(behandlingId)
            behandlingstatistikkRepository.lagre(behandlingId, behandlingDVH, Hendelse.PÅBEGYNT)
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
                             avsender = "NAV Enslig forelder")
    }
}