package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer, private val featureToggleService: FeatureToggleService) {

    fun sendTilKafka(iverksett: Iverksett, forrigeIverksett: Iverksett?) {
        // Kunne ikke bruke sealed class i kontrakt mot datavarehus og det blir derfor if-else her
        if (iverksett is IverksettOvergangsstønad) {
            val vedtakstatistikk = VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(iverksett, forrigeIverksett?.behandling?.eksternId)
            vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
        } else if (iverksett is IverksettBarnetilsyn) {
            val vedtakstatistikk = VedtakstatistikkMapper.mapTilVedtakBarnetilsynDVH(iverksett, forrigeIverksett?.behandling?.eksternId)
            vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
        } else if (iverksett is IverksettSkolepenger) {
            if (featureToggleService.isEnabled("familie.ef.iverksett.vedtaksstatistikk-skolepenger")) {
                val vedtakstatistikk = VedtakstatistikkMapper.mapTilVedtakSkolepengeDVH(iverksett, forrigeIverksett?.behandling?.eksternId)
                vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
            } else {
                error("Featuretoggle \"familie.ef.iverksett.vedtaksstatistikk-skolepenger\" ikke påskrudd ")
            }
        }
    }
}
