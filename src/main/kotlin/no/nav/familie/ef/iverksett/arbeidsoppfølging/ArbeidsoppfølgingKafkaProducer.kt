package no.nav.familie.ef.iverksett.arbeidsoppfølging

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.stereotype.Service

@Service
class ArbeidsoppfølgingKafkaProducer(private val kafkaProducerService: KafkaProducerService) {

    /** TODO : Finne @Value for topic */
    lateinit var topic: String

    fun sendVedtak(vedtak: VedtakArbeidsoppfølging) {
        kafkaProducerService.send(topic, vedtak.behandlingId.toString(), objectMapper.writeValueAsString(vedtak))
    }
}


