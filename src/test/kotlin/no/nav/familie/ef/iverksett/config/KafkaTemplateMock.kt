package no.nav.familie.ef.iverksett.config

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-kafkatemplate")
class KafkaTemplateMock {
    val payloadSlot = slot<String>()

    @Bean
    @Primary
    fun kafkaProducerService(): KafkaProducerService {
        val kafkaProducer = mockk<KafkaProducerService>(relaxed = true)
        every {
            kafkaProducer.send(any(), any(), capture(payloadSlot))
        } answers {
            println("Sender denne payloaden til kafkaproducer: ${payloadSlot.captured}")
        }
        return kafkaProducer
    }

    @Bean
    fun kafkaProducerPayloadSlot(): CapturingSlot<String> = payloadSlot
}
