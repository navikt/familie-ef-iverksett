package no.nav.familie.ef.iverksett.infrastruktur.service

import no.nav.familie.eksterne.kontrakter.ef.StønadType
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    fun send(
        topic: String,
        key: String,
        payload: String,
    ) {
        kafkaTemplate.send(topic, key, payload).get()
    }

    fun sendMedStønadstypeIHeader(
        topic: String,
        stønadstype: StønadType,
        key: String,
        payload: String,
    ) {
        val record = ProducerRecord(topic, key, payload)
        record.headers().add(RecordHeader("stønadstype", stønadstype.name.toByteArray()))
        kafkaTemplate.send(record).get()
    }
}
