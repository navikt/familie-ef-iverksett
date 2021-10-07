package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(iverksett: Iverksett, tilkjentYtelse: TilkjentYtelse?) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett, tilkjentYtelse)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett, tilkjentYtelse: TilkjentYtelse?): BehandlingDVH {
        return BehandlingDVHMapper.map(iverksett, tilkjentYtelse)

    }
}