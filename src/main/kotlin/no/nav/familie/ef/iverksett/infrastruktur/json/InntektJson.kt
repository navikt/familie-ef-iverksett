package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.InntektsType

data class InntektJson(
    val periodebeløp: PeriodebeløpJson,
    val inntektstype: InntektsType
)

data class InntektsreduksjonJson(val periodebeløp: List<PeriodebeløpJson> = emptyList())

