package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksett.domene.Inntekt
import no.nav.familie.ef.iverksett.iverksett.domene.Inntektsreduksjon
import no.nav.familie.kontrakter.ef.iverksett.InntektDto
import no.nav.familie.kontrakter.ef.iverksett.InntektsreduksjonDto

fun InntektsreduksjonDto.toDomain(): Inntektsreduksjon {
    return Inntektsreduksjon(this.periodebeløp.map { it.toDomain() })
}

fun InntektDto.toDomain(): Inntekt {
    return Inntekt(this.periodebeløp.toDomain(), this.inntektstype)
}