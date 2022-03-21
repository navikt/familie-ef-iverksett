package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.toJson
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(
        private val kafkaTemplate: KafkaOperations<String, String>,
        @Value("\${ENSLIG_FORSORGER_VEDTAK_TOPIC}") val topic: String
) {

    fun sendTilKafka(iverksett: Iverksett, forrigeIverksett: Iverksett?) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett, forrigeIverksett)

        kafkaTemplate.send(topic, vedtakstatistikk.behandlingId.toString(), vedtakstatistikk.toJson())
                .addCallback(VedtakstatistikkCallback(vedtakstatistikk))
    }

    private fun hentBehandlingDVH(iverksett: Iverksett, forrigeIverksett: Iverksett?): BehandlingDVH {
        return BehandlingDVHMapper.map(iverksett, forrigeIverksett)
    }
}