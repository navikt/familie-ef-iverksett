package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.familie.ef.iverksett.vedtakstatistikk.toJson
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Stønadstype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.VedtakOvergangsstønadArbeidsoppfølging
import no.nav.familie.eksterne.kontrakter.ef.StønadType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ArbeidsoppfølgingKafkaProducer(
    private val kafkaProducerService: KafkaProducerService,
) {
    @Value("\${ARBEIDSOPPFOLGING_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakOvergangsstønadArbeidsoppfølging: VedtakOvergangsstønadArbeidsoppfølging) {
        sendVedtak(
            vedtakOvergangsstønadArbeidsoppfølging.vedtakId,
            vedtakOvergangsstønadArbeidsoppfølging.stønadstype,
            vedtakOvergangsstønadArbeidsoppfølging.toJson(),
        )
    }

    fun sendVedtak(
        behandlingId: Long,
        stønadstype: Stønadstype,
        vedtakArbeidsoppfølging: String,
    ) {
        logger.info("Sending to Kafka topic: {}", topic)
        secureLogger.info("Sending to Kafka topic: {}\nArbeidsoppfølging: {}", topic, vedtakArbeidsoppfølging)

        runCatching {
            kafkaProducerService.sendMedStønadstypeIHeader(
                topic,
                StønadType.valueOf(stønadstype.name),
                behandlingId.toString(),
                vedtakArbeidsoppfølging,
            )
            logger.info("Arbeidsoppfølging for behandling=$behandlingId sent til Kafka")
        }.onFailure {
            val errorMessage = "Kunne ikke sende vedtak til arbeidsoppfølging topic. Se securelogs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende vedtak til arbeidsoppfølging topic", it)
            throw RuntimeException(errorMessage)
        }
    }
}
