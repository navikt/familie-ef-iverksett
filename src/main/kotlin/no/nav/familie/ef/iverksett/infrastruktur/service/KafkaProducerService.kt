package no.nav.familie.ef.iverksett.infrastruktur.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(private val kafkaTemplate: KafkaTemplate<String, String>) {

    fun send(topic: String, key: String, payload: String) {
        kafkaTemplate.send(topic, key, payload).get()
    }
}