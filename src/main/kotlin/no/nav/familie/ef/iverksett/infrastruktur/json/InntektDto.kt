package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Inntekt
import no.nav.familie.ef.iverksett.domene.InntektsType
import no.nav.familie.ef.iverksett.domene.Inntektsreduksjon

data class InntektDto(
        val periodebeløp: PeriodebeløpDto,
        val inntektstype: InntektsType
)

data class InntektsreduksjonDto(val periodebeløp: List<PeriodebeløpDto> = emptyList())

fun InntektsreduksjonDto.toDomain(): Inntektsreduksjon {
    return Inntektsreduksjon(this.periodebeløp.map { it.toDomain() })
}

fun InntektDto.toDomain(): Inntekt {
    return Inntekt(this.periodebeløp.toDomain(), this.inntektstype)
}
