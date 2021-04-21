package no.nav.familie.ef.iverksett.infrastruktur.json

data class BrevJson(
    val journalpostId: String,
    val brevdata: BrevdataJson
)

data class BrevdataJson(
    val pdf: String,
    val mottaker: String,
    val saksbehandler: String
)
