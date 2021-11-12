package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandlingRespons
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TilbakekrevingProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${FAGSYSTEMBEHANDLING_RESPONS_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun send(behandling: HentFagsystemsbehandlingRespons, key: String) {
        try {
            kafkaProducerService.send(topic, key, behandling.toJson())
            secureLogger.info("Fagsystembehandling er sent til Kafka. Behandling=$behandling")
        } catch (ex: Exception) {
            val errorMessage = "Kunne ikke sende behandling til Kafka. Se securelogs for mere informasjon. "
            logger.error(errorMessage)
            throw RuntimeException(errorMessage)
        }
    }

    private fun Any.toJson(): String = objectMapper.writeValueAsString(this)
}