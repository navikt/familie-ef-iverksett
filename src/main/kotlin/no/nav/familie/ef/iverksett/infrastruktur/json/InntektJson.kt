package no.nav.familie.ef.iverksett.infrastruktur.json

data class InntektJson(
    val periodeBeløp: PeriodeBeløpJson,
    val inntektstype: InntektstypeJson
)

data class InntektsreduksjonJson(val periodeBeløp: List<PeriodeBeløpJson> = emptyList())

enum class InntektstypeJson {
    ARBEIDINNTEKT,
    KAPITALINNTEKT,
    TRYGD_ELLER_STØNAD
}