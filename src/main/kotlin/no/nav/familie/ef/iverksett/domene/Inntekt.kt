package no.nav.familie.ef.iverksett.domene

import no.nav.familie.ef.iverksett.infrastruktur.json.PeriodebeløpJson

data class Inntekt(
    val periodeBeløp: PeriodeBeløp,
    val inntektstype: InntektsType
)

data class Inntektsreduksjon(val periodebeløp: List<PeriodebeløpJson> = emptyList())

enum class InntektsType {
    ARBEIDINNTEKT,
    KAPITALINNTEKT,
    TRYGD_ELLER_STØNAD
}