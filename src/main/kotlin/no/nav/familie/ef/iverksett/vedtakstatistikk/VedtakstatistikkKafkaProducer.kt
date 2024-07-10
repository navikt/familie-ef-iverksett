package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.eksterne.kontrakter.ef.StønadType
import no.nav.familie.eksterne.kontrakter.ef.VedtakBarnetilsynDVH
import no.nav.familie.eksterne.kontrakter.ef.VedtakOvergangsstønadDVH
import no.nav.familie.eksterne.kontrakter.ef.VedtakSkolepenger
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkKafkaProducer(
    private val kafkaProducerService: KafkaProducerService,
) {
    @Value("\${ENSLIG_FORSORGER_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakstatistikk: VedtakOvergangsstønadDVH) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }

    fun sendVedtak(vedtakstatistikk: VedtakBarnetilsynDVH) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }

    fun sendVedtak(vedtakstatistikk: VedtakSkolepenger) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }

    fun sendVedtak(
        behandlingId: Long,
        stønadstype: StønadType,
        vedtakStatistikk: String,
    ) {
        logger.info("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nVedtakStatistikk: {}", topic, vedtakStatistikk)

        runCatching {
            kafkaProducerService.sendMedStønadstypeIHeader(topic, stønadstype, behandlingId.toString(), vedtakStatistikk)
            logger.info("Vedtakstatistikk for behandling=$behandlingId sent til Kafka")
        }.onFailure {
            val errorMessage = "Could not send vedtak to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send vedtak to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }
}

fun Any.toJson(): String = objectMapper.writeValueAsString(this)
