package no.nav.familie.ef.iverksett.vedtakstatistikk

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID


internal class VedtakstatistikkTaskTest{
    val iverksettingRepository = mockk<IverksettingRepository>()
    val vedtakstatistikkService = mockk<VedtakstatistikkService>()
    val vedtakstatistikkTask = VedtakstatistikkTask(iverksettingRepository, vedtakstatistikkService)
    val behandlingId = UUID.randomUUID()


    @Test
    fun `skal sende vedtaksstatistikk til DVH`(){
        val behandlingIdString = behandlingId.toString()
        every { vedtakstatistikkService.sendTilKafka(any()) } just Runs
        every { iverksettingRepository.hent(behandlingId) }.returns(opprettIverksettDto(behandlingId = behandlingId).toDomain())
        vedtakstatistikkTask.doTask(Task(VedtakstatistikkTask.TYPE, behandlingIdString, Properties()))
        verify(exactly = 1) { vedtakstatistikkService.sendTilKafka(any()) }
    }
}