package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(iverksett: Iverksett, forrigeIverksett: Iverksett?) {
        val vedtakstatistikk = mapTilVedtakstatistikk(iverksett, forrigeIverksett)

        vedtakstatistikkKafkaProducer.sendVedtak(iverksett.behandling.behandlingId.toString(), vedtakstatistikk)
    }

    private fun mapTilVedtakstatistikk(iverksett: Iverksett, forrigeIverksett: Iverksett?): String {
        return BehandlingDVHMapper.mapIverksettTilVedtakstatistikkJson(iverksett, forrigeIverksett)
    }
}