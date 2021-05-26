package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.iverksett.Periodebeløp
import no.nav.familie.ef.iverksett.iverksett.Periodetype
import java.time.LocalDate

data class PeriodebeløpDto(

        val beløp: Int,
        var periodetype: Periodetype,
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
)

fun PeriodebeløpDto.toDomain(): Periodebeløp {
    return Periodebeløp(
            this.beløp,
            this.periodetype,
            this.fraOgMed,
            this.tilOgMed
    )
}