package no.nav.familie.ef.iverksett.tilbakekreving

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandlingRespons
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate

internal class TilbakekrevingProducerTest {

    private val tilbakekrevingProducer = mockk<TilbakekrevingProducer>()
    private val kafkaTemplateMock = mockk<KafkaTemplate<String, String>>()
    private val behandling = mockk<HentFagsystemsbehandlingRespons>()

    private lateinit var producer: TilbakekrevingProducer

    @BeforeEach
    internal fun setUp() {
        producer = TilbakekrevingProducer(kafkaTemplateMock)
    }

    @Test
    internal fun `kast unntak ved sending av melding, forvent at unntak viderekastes`() {
        every { kafkaTemplateMock.send(any(), any(), any()) } throws RuntimeException()
        assertThrows(RuntimeException::class.java) { tilbakekrevingProducer.send(behandling, "0") }
    }

}