package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.felles.jsonMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BehandlingsstatistikkProducer(
    private val kafkaProducerService: KafkaProducerService,
) {
    @Value("\${ENSLIG_FORSORGER_BEHANDLING_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBehandling(behandlingDvh: BehandlingDVH) {
        runCatching {
            kafkaProducerService.send(topic, behandlingDvh.behandlingId.toString(), behandlingDvh.toJson())
            logger.info(
                "Behandlingstatistikk for behandling=${behandlingDvh.behandlingId} " +
                    "behandlingStatus=${behandlingDvh.behandlingStatus} sendt til Kafka",
            )
        }.onFailure {
            val errorMessage = "Kunne ikke sende behandlingsstatistikk til topic: $topic. Se securelogs for mer info."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende behandlingsstatistikk til topic: $topic", it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun Any.toJson(): String = jsonMapper.writeValueAsString(this)
}
