package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BehandlingstatistikkProducer(
        private val kafkaTemplate: KafkaTemplate<String, String>
) {

    @Value("\${ENSLIG_FORSORGER_BEHANDLING_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBehandling(behandlingDvh: BehandlingDVH) {
        logger.info("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nBehandlingstatistikk: {}", topic, behandlingDvh)
        runCatching {
            kafkaTemplate.send(topic, behandlingDvh.toJson())
            logger.info("Behandlingstatistikk sent to Kafka")
            secureLogger.info("$behandlingDvh sent to Kafka.")
        }.onFailure {
            val errorMessage = "Could not send behandling to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send behandling to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun Any.toJson(): String = objectMapper.writeValueAsString(this)
}