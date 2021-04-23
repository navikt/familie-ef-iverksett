package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Brevdata

data class BrevJson(
    val journalpostId: String,
    val brevdata: BrevdataJson
)

data class BrevdataJson(
    val mottaker: String,
    val saksbehandler: String
)

fun BrevdataJson.toDomain(): Brevdata {
    return Brevdata(this.mottaker, this.saksbehandler)
}

fun BrevJson.toDomain(): Brev {
    return Brev(this.journalpostId, this.brevdata.toDomain())
}