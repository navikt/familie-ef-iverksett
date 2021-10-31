package no.nav.familie.ef.iverksett.tilbakekreving

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandling
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TilbakekrevingProducerTest {

    private val tilbakekrevingProducer = mockk<TilbakekrevingProducer>()
    private val kafkaProducerService = mockk<KafkaProducerService>()
    private val behandling = mockk<HentFagsystemsbehandling>()

    private lateinit var producer: TilbakekrevingProducer

    @BeforeEach
    internal fun setUp() {
        producer = TilbakekrevingProducer(kafkaProducerService)
    }

    @Test
    internal fun `kast unntak ved sending av melding, forvent at unntak viderekastes`() {
        every { kafkaProducerService.send(any(), any(), any()) } throws RuntimeException()
        assertThrows(RuntimeException::class.java) { tilbakekrevingProducer.send(behandling) }
    }

}