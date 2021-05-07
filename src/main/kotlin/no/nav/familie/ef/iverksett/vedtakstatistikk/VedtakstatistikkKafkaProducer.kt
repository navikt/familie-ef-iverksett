package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, BehandlingDVH>
) {
    @Value("\${ENSLIG_FORSORGER_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakStatistikk: BehandlingDVH) {
        logger.info("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nVedtakStatistikk: {}", topic, vedtakStatistikk)
        runCatching {
            kafkaTemplate.send(topic, vedtakStatistikk)
            logger.info("$vedtakStatistikk sent to Kafka.")
        }.onFailure {
            val errorMessage = "Could not send vedtak to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send vedtak to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }
}