package no.nav.familie.ef.iverksett.domene

import java.time.LocalDate

data class Periodebeløp(

    val utbetaltPerPeriode: Int,
    var periodetype: Periodetype,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
)

enum class Periodetype {
    MÅNED
}