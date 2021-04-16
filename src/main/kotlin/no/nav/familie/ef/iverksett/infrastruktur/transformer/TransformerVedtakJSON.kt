package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.Vedtak
import no.nav.familie.ef.iverksett.infrastruktur.json.VedtakJSON

class TransformerVedtakJSON {

    companion object {

        fun transformer(vedtakJSON: VedtakJSON): Vedtak {
            return Vedtak(vedtakJSON.godkjent, vedtakJSON.begrunnelse)
        }
    }
}