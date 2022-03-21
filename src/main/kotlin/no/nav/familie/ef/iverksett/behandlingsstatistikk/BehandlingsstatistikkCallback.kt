package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.exception.IntegrationException
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class BehandlingsstatistikkCallback(val behandlingDvh: BehandlingDVH) :
        ListenableFutureCallback<SendResult<String, String>> {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun onSuccess(result: SendResult<String, String>?) {
        logger.info("Behandlingstatistikk for behandling=${behandlingDvh.behandlingId} " +
                    "behandlingStatus=${behandlingDvh.behandlingStatus} sendt til Kafka" +
                    "på topic {}, partition {} med offset {} OK",
                    result?.recordMetadata?.topic(),
                    result?.recordMetadata?.partition(),
                    result?.recordMetadata?.offset())
        secureLogger.debug("Behandlingsstatistikk $behandlingDvh sent til kafka ($result)")
    }

    override fun onFailure(e: Throwable) {
        logger.error("Klarte ikke sende behandlingstatistikk med behandlingId ${behandlingDvh.behandlingId} " +
                     "og behandlingStatus ${behandlingDvh.behandlingStatus} til Kafka, se securelog for info")
        secureLogger.error("Klarte ikke sende $behandlingDvh til Kafka", e)
        throw IntegrationException("Klarte ikke sende inn behandlingstatistikk", e)
    }
}