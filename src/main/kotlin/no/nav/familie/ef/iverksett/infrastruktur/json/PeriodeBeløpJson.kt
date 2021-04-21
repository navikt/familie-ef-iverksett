package no.nav.familie.ef.iverksett.infrastruktur.json

import java.time.LocalDate

data class PeriodeBeløpJson(

    val utbetaltPerPeriode: Int,
    var periodetype: PeriodetypeJson,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
)

enum class PeriodetypeJson {
    MÅNED
}