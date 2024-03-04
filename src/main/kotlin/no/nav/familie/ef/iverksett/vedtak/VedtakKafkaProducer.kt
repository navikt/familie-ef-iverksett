package no.nav.familie.ef.iverksett.vedtak

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.kontrakter.felles.ef.EnsligForsørgerVedtakhendelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VedtakKafkaProducer(private val kafkaProducerService: KafkaProducerService) {
    @Value("\${VEDTAK_TOPIC}")
    lateinit var topic: String

    fun sendVedtak(hendelse: EnsligForsørgerVedtakhendelse) {
        kafkaProducerService.send(topic, hendelse.behandlingId.toString(), objectMapper.writeValueAsString(hendelse))
    }
}
