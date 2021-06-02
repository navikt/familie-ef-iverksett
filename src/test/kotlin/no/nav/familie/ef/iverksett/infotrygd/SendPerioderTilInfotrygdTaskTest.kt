package no.nav.familie.ef.iverksett.infotrygd

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SendPerioderTilInfotrygdTaskTest {


    private val infotrygdFeedClient = mockk<InfotrygdFeedClient>(relaxed = true)
    private val iverksettingRepository = mockk<IverksettingRepository>()

    private val behandlingId = UUID.randomUUID()

    @Test
    internal fun `skal sende perioder til infotrygd`() {
        val task = SendPerioderTilInfotrygdTask(infotrygdFeedClient, iverksettingRepository, "dev")
        every { iverksettingRepository.hent(behandlingId) } returns opprettIverksett(behandlingId)

        task.doTask(Task(SendPerioderTilInfotrygdTask.TYPE, behandlingId.toString()))

        verify(exactly = 1) { infotrygdFeedClient.opprettPeriodeHendelse(any()) }
    }

    @Test
    internal fun `skal kaste feil hvis det er prod`() {
        val task = SendPerioderTilInfotrygdTask(infotrygdFeedClient, iverksettingRepository, "prod")
        assertThat(catchThrowable { task.doTask(Task(SendPerioderTilInfotrygdTask.TYPE, behandlingId.toString())) })
                .hasMessageContaining("Må håndtere fullOvergangsstønad før denne kjøres i prod")
    }
}