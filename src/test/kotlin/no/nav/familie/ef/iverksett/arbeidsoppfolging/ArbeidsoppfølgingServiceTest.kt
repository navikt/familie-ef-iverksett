package no.nav.familie.ef.iverksett.arbeidsoppfolging

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.util.opprettIverksettBarnetilsyn
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import org.junit.Test

class ArbeidsoppfølgingServiceTest {

    private val arbeidsoppfølgingKafkaProducer = mockk<ArbeidsoppfølgingKafkaProducer>()

    val arbeidsoppfølgingService = ArbeidsoppfølgingService(arbeidsoppfølgingKafkaProducer)

    @Test
    fun `sendTilKafka hvis overgangsstønad`() {
        justRun { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }
        val iverksett = opprettIverksettOvergangsstønad()
        arbeidsoppfølgingService.sendTilKafka(iverksett)

        verify(exactly = 1) { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }
    }

    @Test
    fun `ikke sendTilKafka hvis barnetilsyn eller skolepenger`() {
        justRun { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }
        val iverksettBarnetilsyn = opprettIverksettBarnetilsyn()
        arbeidsoppfølgingService.sendTilKafka(iverksettBarnetilsyn)

        verify(exactly = 0) { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }

        arbeidsoppfølgingService.sendTilKafka(IverksettSkolepenger(mockk(), mockk(), mockk(), mockk()))
        verify(exactly = 0) { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }
    }
}
