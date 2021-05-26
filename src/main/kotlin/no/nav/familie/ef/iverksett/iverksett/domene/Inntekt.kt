package no.nav.familie.ef.iverksett.iverksett.domene

data class Inntekt(
        val periodebeløp: Periodebeløp,
        val inntektstype: InntektsType? = null // TODO: Dette har vi ikke
)

data class Inntektsreduksjon(
    val periodebeløp: List<Periodebeløp> = emptyList()
)

enum class InntektsType {
    ARBEIDINNTEKT,
    KAPITALINNTEKT,
    TRYGD_ELLER_STØNAD
}