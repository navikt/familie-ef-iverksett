package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Utbetaling
import no.nav.familie.ef.iverksett.domene.Utbetalingsdetalj

data class UtbetalingJson(
    val periodebeløp: PeriodebeløpJson,
    val utbetalingsdetalj: UtbetalingsdetaljJson
)

data class UtbetalingsdetaljJson(
    val gjelderPerson: PersonJson,
    val klassekode: String,
    val delytelseId: String
)

fun UtbetalingJson.toDomain(): Utbetaling {
    return Utbetaling(this.periodebeløp.toDomain(), this.utbetalingsdetalj.toDomain())
}

fun UtbetalingsdetaljJson.toDomain(): Utbetalingsdetalj {
    return Utbetalingsdetalj(this.gjelderPerson.toDomain(), this.klassekode, this.delytelseId)
}