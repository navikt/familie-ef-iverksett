package no.nav.familie.ef.iverksett.infotrygd

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SendPerioderTilInfotrygdTaskTest {


    private val infotrygdFeedClient = mockk<InfotrygdFeedClient>(relaxed = true)
    private val iverksettingRepository = mockk<IverksettingRepository>()

    private val task = SendPerioderTilInfotrygdTask(infotrygdFeedClient, iverksettingRepository)

    private val behandlingId = UUID.randomUUID()

    @Test
    internal fun `skal sende perioder til infotrygd`() {
        every { iverksettingRepository.hent(behandlingId) } returns opprettIverksett(behandlingId)

        task.doTask(Task(SendPerioderTilInfotrygdTask.TYPE, behandlingId.toString()))

        verify(exactly = 1) { infotrygdFeedClient.opprettPeriodeHendelse(any()) }
    }
}