package no.nav.familie.ef.iverksett.config

import io.mockk.Runs
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFuture

@Configuration
@Profile("mock-kafkatemplate")
class KafkaTemplateMock {

    @Bean
    @Primary
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        val kafkaTemplate = mockk<KafkaTemplate<String, String>>(relaxed = true)
        val listenableFuture = mockk<ListenableFuture<SendResult<String, String>>>()
        val sendResult = mockk<SendResult<String, String>>()
        every { kafkaTemplate.send(any(), any()) } returns listenableFuture
        every { listenableFuture.get() } returns sendResult
        return kafkaTemplate
    }
}
