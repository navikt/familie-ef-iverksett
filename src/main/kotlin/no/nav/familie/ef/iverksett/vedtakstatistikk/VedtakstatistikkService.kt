package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(
    private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer
) {

    fun sendTilKafka(iverksettData: IverksettData, forrigeIverksett: IverksettData?) {
        // Kunne ikke bruke sealed class i kontrakt mot datavarehus og det blir derfor if-else her
        when (iverksettData) {
            is IverksettOvergangsstønad -> vedtakstatistikkKafkaProducer.sendVedtak(
                VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(
                    iverksettData,
                    forrigeIverksett?.behandling?.eksternId
                )
            )
            is IverksettBarnetilsyn -> vedtakstatistikkKafkaProducer.sendVedtak(
                VedtakstatistikkMapper.mapTilVedtakBarnetilsynDVH(
                    iverksettData,
                    forrigeIverksett?.behandling?.eksternId
                )
            )
            is IverksettSkolepenger -> vedtakstatistikkKafkaProducer.sendVedtak(
                VedtakstatistikkMapper.mapTilVedtakSkolepengeDVH(
                    iverksettData,
                    forrigeIverksett?.behandling?.eksternId
                )
            )
        }
    }
}
