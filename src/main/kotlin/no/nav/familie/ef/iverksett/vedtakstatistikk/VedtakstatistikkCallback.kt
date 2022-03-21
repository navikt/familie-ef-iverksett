package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.exception.IntegrationException
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class VedtakstatistikkCallback(val behandlingDvh: BehandlingDVH) :
        ListenableFutureCallback<SendResult<String, String>> {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun onSuccess(result: SendResult<String, String>?) {
        logger.info("Vedtakstatistikk for behandling=${behandlingDvh.behandlingId} sendt til Kafka" +
                    "på topic {}, partition {} med offset {} OK",
                    result?.recordMetadata?.topic(),
                    result?.recordMetadata?.partition(),
                    result?.recordMetadata?.offset())
        secureLogger.debug("Vedtakstatistikk $behandlingDvh sendt til kafka ($result)")
    }

    override fun onFailure(e: Throwable) {
        logger.error("Klarte ikke sende vedtakstatistikk med behandlingId ${behandlingDvh.behandlingId} " +
                     "til Kafka, se securelog for info")
        secureLogger.error("Klarte ikke sende $behandlingDvh til Kafka", e)
        throw IntegrationException("Klarte ikke sende inn vedtakstatistikk", e)
    }
}