package no.nav.familie.ef.iverksett.infrastruktur.configuration

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
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
    fun kafkaTemplate(properties: KafkaProperties): KafkaTemplate<String, String> {
        val producerListener = LoggingProducerListener<String, String>()
        producerListener.setIncludeContents(false)
        val producerFactory = DefaultKafkaProducerFactory<String, String>(properties.buildProducerProperties())

        return KafkaTemplate(producerFactory).apply<KafkaTemplate<String, String>> {
            setProducerListener(producerListener)
        }
    }

    @Bean
    fun kafkaTemplateBrukerNotifikasjoner(properties: KafkaProperties): KafkaTemplate<NokkelInput, BeskjedInput> {
        val producerListener = LoggingProducerListener<NokkelInput, BeskjedInput>()
        producerListener.setIncludeContents(false)
        val producerFactory = DefaultKafkaProducerFactory<NokkelInput, BeskjedInput>(properties.buildProducerProperties().apply {
            put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
            put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO")
            put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, "$schemaRegistryUser:$schemaRegistryPassword",)
        })

        return KafkaTemplate(producerFactory).apply<KafkaTemplate<NokkelInput, BeskjedInput>> {
            setProducerListener(producerListener)
        }
    }
}
