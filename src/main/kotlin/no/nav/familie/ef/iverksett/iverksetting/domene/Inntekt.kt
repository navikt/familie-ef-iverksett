package no.nav.familie.ef.iverksett.iverksetting.domene

import java.time.LocalDate

data class Inntekt(
        val beløp: Int,
        val samordningsfradrag: Int,
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
)

