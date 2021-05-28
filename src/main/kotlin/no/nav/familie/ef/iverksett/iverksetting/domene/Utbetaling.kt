package no.nav.familie.ef.iverksett.iverksetting.domene

data class Utbetaling(
        val periodebeløp: Periodebeløp,
        val utbetalingsdetalj: Utbetalingsdetalj
)

data class Utbetalingsdetalj(
        val gjelderPerson: Person,
        val klassekode: String,
        val delytelseId: String
)
