package no.nav.familie.ef.iverksett.config

import io.mockk.justRun
import io.mockk.mockk
import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-kafkatemplate")
class KafkaTemplateMock {

    @Bean
    @Primary
    fun kafkaProducerService(): KafkaProducerService {
        val kafkaProducer = mockk<KafkaProducerService>(relaxed = true)
        justRun { kafkaProducer.send(any(), any()) }
        return kafkaProducer
    }
}
