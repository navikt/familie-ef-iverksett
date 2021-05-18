package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettDto
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur.LagreIverksettJdbc
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LagreIverksettServiceTest : ServerTest() {

    private lateinit var lagreIverksettJdbc: LagreIverksettJdbc
    private lateinit var lagreIverksettService: LagreIverksettService

    @BeforeEach
    fun setUp() {
        lagreIverksettJdbc = mockk()
        lagreIverksettService = LagreIverksettService(lagreIverksettJdbc)
    }

    @Test
    internal fun `lagre iverksett data og brev`() {
        val behandlingId = UUID.randomUUID()
        val iverksettDto: IverksettDto = opprettIverksettDto(behandlingId)

        every { lagreIverksettJdbc.lagre(any(), any(), any()) } returns Unit

        val iverksett = iverksettDto.toDomain()
        val brev = opprettBrev()

        lagreIverksettService.lagreIverksett(
            behandlingId = behandlingId,
            iverksett = iverksett,
            brev = brev
        )

        verify(exactly = 1) { lagreIverksettJdbc.lagre(behandlingId, iverksett, brev) }
    }
}