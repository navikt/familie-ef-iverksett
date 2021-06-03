package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import java.time.LocalDate

data class Inntekt(
        val beløp: Int,
        val samordningsfradrag: Int,
        var periodetype: Periodetype,
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
)

