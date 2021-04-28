package no.nav.familie.ef.iverksett.domene

import org.springframework.data.relational.core.mapping.MappedCollection

data class Inntekt(
    val periodebeløp: Periodebeløp,
    val inntektstype: InntektsType
)

data class Inntektsreduksjon(
    @MappedCollection(idColumn = "inntektsreduksjon_id")
    val periodebeløp: List<Periodebeløp> = emptyList()
)

enum class InntektsType {
    ARBEIDINNTEKT,
    KAPITALINNTEKT,
    TRYGD_ELLER_STØNAD
}