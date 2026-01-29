package no.nav.familie.ef.iverksett.infrastruktur.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.LoggingProducerListener

@Configuration
class KafkaConfig {
    @Value("\${KAFKA_SCHEMA_REGISTRY}")
    lateinit var schemaRegistryUrl: String

    @Value("\${KAFKA_SCHEMA_REGISTRY_USER}")
    lateinit var schemaRegistryUser: String

    @Value("\${KAFKA_SCHEMA_REGISTRY_PASSWORD}")
    lateinit var schemaRegistryPassword: String

    @Bean
    fun kafkaTemplate(
        properties: KafkaProperties,
    ): KafkaTemplate<String, String> {
        val producerListener = LoggingProducerListener<String, String>()
        producerListener.setIncludeContents(false)
        val producerFactory = DefaultKafkaProducerFactory<String, String>(properties.buildProducerProperties())

        return KafkaTemplate(producerFactory).apply<KafkaTemplate<String, String>> {
            setProducerListener(producerListener)
        }
    }
}
