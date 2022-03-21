package no.nav.familie.ef.iverksett.vedtak

import no.nav.familie.ef.iverksett.util.toJson
import no.nav.familie.kontrakter.felles.ef.EnsligForsørgerVedtakhendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class VedtakKafkaProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    @Value("\${VEDTAK_TOPIC}")
    lateinit var topic: String

    fun sendVedtak(hendelse: EnsligForsørgerVedtakhendelse) {
        kafkaTemplate.send(topic, hendelse.behandlingId.toString(), hendelse.toJson())
    }
}
