package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Inntekt
import no.nav.familie.ef.iverksett.domene.InntektsType
import no.nav.familie.ef.iverksett.domene.Inntektsreduksjon

data class InntektJson(
    val periodebeløp: PeriodebeløpJson,
    val inntektstype: InntektsType
)

data class InntektsreduksjonJson(val periodebeløp: List<PeriodebeløpJson> = emptyList())

fun InntektsreduksjonJson.toDomain(): Inntektsreduksjon {
    return Inntektsreduksjon(this.periodebeløp.map { it.toDomain() }.toList())
}

fun InntektJson.toDomain(): Inntekt {
    return Inntekt(this.periodebeløp.toDomain(), this.inntektstype)
}

fun Inntektsreduksjon.toJson(): InntektsreduksjonJson {
    return InntektsreduksjonJson(this.periodebeløp.map { it.toJson() }.toList())
}

fun Inntekt.toJson(): InntektJson {
    return InntektJson(this.periodebeløp.toJson(), this.inntektstype)
}
