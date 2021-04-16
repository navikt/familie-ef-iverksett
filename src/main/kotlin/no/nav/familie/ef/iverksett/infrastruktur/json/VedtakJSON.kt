package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.annotation.JsonProperty

data class VedtakJSON(

        @JsonProperty("godkjent")
        val godkjent: Boolean,

        @JsonProperty("begrunnelse")
        val begrunnelse: String?

)
