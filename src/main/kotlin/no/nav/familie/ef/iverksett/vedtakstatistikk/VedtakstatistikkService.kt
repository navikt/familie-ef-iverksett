package no.nav.familie.ef.iverksett.vedtakstatistikk

import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(vedtakStatistikk: String) {
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakStatistikk)
    }
}