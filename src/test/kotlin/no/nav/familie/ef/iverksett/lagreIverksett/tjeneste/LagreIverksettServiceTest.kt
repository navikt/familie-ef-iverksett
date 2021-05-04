package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur.LagreIverksettJdbc
import no.nav.familie.ef.iverksett.util.opprettIverksettJson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LagreIverksettServiceTest : ServerTest() {

    private lateinit var lagreIverksett: LagreIverksett
    private lateinit var lagreIverksettJdbc: LagreIverksettJdbc
    private lateinit var lagreIverksettService: LagreIverksettService

    @BeforeEach
    fun setUp() {
        lagreIverksett = mockk()
        lagreIverksettJdbc = mockk()
        lagreIverksettService = LagreIverksettService(lagreIverksett)
    }

    @Test
    internal fun `lagre iverksett data og brev`() {
        val behandlingId = UUID.randomUUID()
        val iverksettJson = opprettIverksettJson(behandlingId.toString(), emptyList()).toString()

        every { lagreIverksett.lagre(any(), any(), any()) } returns Unit

        lagreIverksettService.lagreIverksettJson(behandlingsId = behandlingId,
                                                 iverksettJson = iverksettJson,
                                                 emptyList())
        verify(exactly = 1) { lagreIverksett.lagre(behandlingId, iverksettJson, emptyList()) }
    }
}