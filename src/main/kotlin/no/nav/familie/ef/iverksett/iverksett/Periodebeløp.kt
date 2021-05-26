package no.nav.familie.ef.iverksett.iverksett

import java.time.LocalDate

data class Periodebeløp(

        val beløp: Int,
        var periodetype: Periodetype,
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
)

enum class Periodetype {
    MÅNED
}