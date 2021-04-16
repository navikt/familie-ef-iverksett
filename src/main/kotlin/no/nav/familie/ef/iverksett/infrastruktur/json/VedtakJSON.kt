package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.ef.iverksett.Vedtak

data class VedtakJSON(

        @JsonProperty("godkjent")
        val godkjent: Boolean,

        @JsonProperty("begrunnelse")
        val begrunnelse: String?

)

inline fun VedtakJSON.transform(): Vedtak {
        return Vedtak(this.godkjent, this.begrunnelse)
}