package no.nav.familie.ef.iverksett.domene

data class Brev(
    val journalpostId: String,
    val brevdata: Brevdata
)

data class Brevdata(
    val pdf: String,
    val mottaker: String,
    val saksbehandler: String
)