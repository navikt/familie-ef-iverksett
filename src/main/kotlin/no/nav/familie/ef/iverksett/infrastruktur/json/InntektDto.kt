package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.iverksett.Inntekt
import no.nav.familie.ef.iverksett.iverksett.InntektsType
import no.nav.familie.ef.iverksett.iverksett.Inntektsreduksjon

data class InntektDto(
        val periodebeløp: PeriodebeløpDto,
        val inntektstype: InntektsType? = null
)

data class InntektsreduksjonDto(val periodebeløp: List<PeriodebeløpDto> = emptyList())

fun InntektsreduksjonDto.toDomain(): Inntektsreduksjon {
    return Inntektsreduksjon(this.periodebeløp.map { it.toDomain() })
}

fun InntektDto.toDomain(): Inntekt {
    return Inntekt(this.periodebeløp.toDomain(), this.inntektstype)
}
