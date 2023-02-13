package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import org.springframework.stereotype.Service

@Service
class ArbeidsoppfølgingService(
    private val arbeidsoppfølgingKafkaProducer: ArbeidsoppfølgingKafkaProducer,
) {

    fun sendTilKafka(iverksettData: IverksettData) {
        if (iverksettData is IverksettOvergangsstønad) {
            arbeidsoppfølgingKafkaProducer.sendVedtak(
                ArbeidsoppfølgingMapper.mapTilVedtakOvergangsstønadTilArbeidsoppfølging(iverksettData),
            )
        }
    }
}
