package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.util.toJson
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandlingRespons
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TilbakekrevingProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    @Value("\${FAGSYSTEMBEHANDLING_RESPONS_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(behandling: HentFagsystemsbehandlingRespons, key: String) {
        try {
            kafkaTemplate.send(topic, key, behandling.toJson())
            logger.info("Fagsystembehandling er sent til Kafka. key=${key} " +
                        "eksternFagsakId=${behandling.hentFagsystemsbehandling?.eksternFagsakId}")
        } catch (ex: Exception) {
            val errorMessage = "Kunne ikke sende behandling til Kafka. Se securelogs for mere informasjon. "
            logger.error(errorMessage)
            throw RuntimeException(errorMessage)
        }
    }
}