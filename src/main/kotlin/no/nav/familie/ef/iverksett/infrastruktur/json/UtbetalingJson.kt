package no.nav.familie.ef.iverksett.infrastruktur.json

data class UtbetalingJson(
    val periodeBeløp: PeriodeBeløpJson,
    val utbetalingsdetalj: UtbetalingsdetaljJson
)

data class UtbetalingsdetaljJson(
    val gjelderPerson: PersonJson, // Identifiserer hvilken person utbetalingen gjelder, ikke nødvendigvis brukeren selv
    val klassekode: String, // Identifiserer detaljert stønadstype i oppdragsystemet: "EFOG", "EFBT" og "EFSP"
    val delytelseId: String
) // Identifiderer utbetalingen i oppdragssystemet
