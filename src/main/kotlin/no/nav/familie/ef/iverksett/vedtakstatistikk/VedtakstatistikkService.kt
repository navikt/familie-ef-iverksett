package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(iverksett: Iverksett, forrigeIverksett: Iverksett?) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett, forrigeIverksett)

        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett, forrigeIverksett: Iverksett?): BehandlingDVH {
        return BehandlingDVHMapper.map(iverksett, forrigeIverksett)

    }
}