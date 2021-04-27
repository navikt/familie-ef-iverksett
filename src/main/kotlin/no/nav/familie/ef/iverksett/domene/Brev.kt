package no.nav.familie.ef.iverksett.domene

import org.springframework.data.relational.core.mapping.Embedded

data class Brev(
        val journalpostId: String,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "brevdata_")
        val brevdata: Brevdata
)

data class Brevdata(
        val mottaker: String,
        val saksbehandler: String,
        val pdf: ByteArray
)