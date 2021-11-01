package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandling
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TilbakekrevingProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${FAGSYSTEMBEHANDLING_RESPONS_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun send(behandling: HentFagsystemsbehandling) {
        secureLogger.debug("Sender til Kafka topic: $topic, Fagsystembehandling=$behandling")
        try {
            kafkaProducerService.send(topic, behandling.eksternId, behandling.toJson())
            logger.info("Fagsystembehandling ifm tilbakekreving er sent til Kafka. EksternId=${behandling.eksternId}")
            secureLogger.info("Fagsystembehandling er sent til Kafka. Behandling=$behandling")
        } catch (ex : Exception) {
            val errorMessage = "Kunne ikke sende behandling til Kafka. Se securelogs for mere informasjon. "
            logger.error(errorMessage)
            throw RuntimeException(errorMessage)
        }
    }

    private fun Any.toJson(): String = objectMapper.writeValueAsString(this)
}