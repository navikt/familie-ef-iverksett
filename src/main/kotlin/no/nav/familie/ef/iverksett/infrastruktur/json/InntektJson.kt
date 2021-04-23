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
    return Inntektsreduksjon(this.periodebeløp.map { it.toDomain() })
}

fun InntektJson.toDomain(): Inntekt {
    return Inntekt(this.periodebeløp.toDomain(), this.inntektstype)
}
