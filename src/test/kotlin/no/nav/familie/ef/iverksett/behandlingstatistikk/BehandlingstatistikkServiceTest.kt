package no.nav.familie.ef.iverksett.behandlingstatistikk

import io.mockk.mockk
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.util.opprettBehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class BehandlingstatistikkServiceTest : ServerTest() {

    val behandlingstatistikkRepository = mockk<BehandlingstatistikkRepository>()

    @Autowired
    lateinit var behandlingstatistikkService: BehandlingstatistikkService

    @Test
    fun `lagre BehandlingsstatistikkDto for hendelse MOTTATT, forvent ingen unntak`() {
        behandlingstatistikkService.lagreBehandlingstatistikk(opprettBehandlingStatistikkDto(UUID.randomUUID(), Hendelse.MOTTATT))
    }

    @Test
    fun `lagre BehandlingsstatistikkDto for alle hendelser i sekvens, forvent ingen unntak`() {
        val uuid = UUID.randomUUID()
        enumValues<Hendelse>().forEach { behandlingstatistikkService.lagreBehandlingstatistikk(opprettBehandlingStatistikkDto(uuid, it)) }

    }
}