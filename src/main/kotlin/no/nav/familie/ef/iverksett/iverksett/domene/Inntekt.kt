package no.nav.familie.ef.iverksett.iverksett.domene

import no.nav.familie.kontrakter.ef.iverksett.InntektsType

data class Inntekt(
        val periodebeløp: Periodebeløp,
        val inntektstype: InntektsType? = null // TODO: Dette har vi ikke
)

data class Inntektsreduksjon(
        val periodebeløp: List<Periodebeløp> = emptyList()
)
