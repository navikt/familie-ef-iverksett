package no.nav.familie.ef.iverksett.infrastruktur.configuration

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.LoggingProducerListener

@Configuration
class KafkaConfig {

    @Bean
    fun kafkaTemplate(properties: KafkaProperties): KafkaTemplate<String, String> {
        val producerListener = LoggingProducerListener<String, String>()
        producerListener.setIncludeContents(false)
        val producerFactory = DefaultKafkaProducerFactory<String, String>(properties.buildProducerProperties())

        return KafkaTemplate(producerFactory).apply<KafkaTemplate<String, String>> {
            setProducerListener(producerListener)
        }
    }
}
