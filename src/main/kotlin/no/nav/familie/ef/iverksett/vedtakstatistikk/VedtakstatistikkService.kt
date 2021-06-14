package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(iverksett: Iverksett) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett): BehandlingDVH {
        return BehandlingDVHMapper.map(iverksett)

    }
}