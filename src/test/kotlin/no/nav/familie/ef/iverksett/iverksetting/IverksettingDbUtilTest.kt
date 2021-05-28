package no.nav.familie.ef.iverksett.iverksetting

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class IverksettingDbUtilTest : ServerTest() {

    private lateinit var iverksettingJdbc: IverksettingJdbc
    private lateinit var iverksettingDbUtil: IverksettingDbUtil

    @BeforeEach
    fun setUp() {
        iverksettingJdbc = mockk()
        iverksettingDbUtil = IverksettingDbUtil(iverksettingJdbc)
    }

    @Test
    internal fun `lagre iverksett data og brev`() {
        val behandlingId = UUID.randomUUID()
        val iverksettDto: IverksettDto = opprettIverksettDto(behandlingId)

        every { iverksettingJdbc.lagre(any(), any(), any()) } returns Unit

        val iverksett = iverksettDto.toDomain()
        val brev = opprettBrev()

        iverksettingDbUtil.lagreIverksett(
            behandlingId = behandlingId,
            iverksett = iverksett,
            brev = brev
        )

        verify(exactly = 1) { iverksettingJdbc.lagre(behandlingId, iverksett, brev) }
    }
}