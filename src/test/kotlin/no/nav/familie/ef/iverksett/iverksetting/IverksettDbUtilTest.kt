package no.nav.familie.ef.iverksett.iverksetting

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettJdbc
import no.nav.familie.ef.iverksett.iverksetting.IverksettDbUtil
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class IverksettDbUtilTest : ServerTest() {

    private lateinit var iverksettJdbc: IverksettJdbc
    private lateinit var iverksettDbUtil: IverksettDbUtil

    @BeforeEach
    fun setUp() {
        iverksettJdbc = mockk()
        iverksettDbUtil = IverksettDbUtil(iverksettJdbc)
    }

    @Test
    internal fun `lagre iverksett data og brev`() {
        val behandlingId = UUID.randomUUID()
        val iverksettDto: IverksettDto = opprettIverksettDto(behandlingId)

        every { iverksettJdbc.lagre(any(), any(), any()) } returns Unit

        val iverksett = iverksettDto.toDomain()
        val brev = opprettBrev()

        iverksettDbUtil.lagreIverksett(
            behandlingId = behandlingId,
            iverksett = iverksett,
            brev = brev
        )

        verify(exactly = 1) { iverksettJdbc.lagre(behandlingId, iverksett, brev) }
    }
}