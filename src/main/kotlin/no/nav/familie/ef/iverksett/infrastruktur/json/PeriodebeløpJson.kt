package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Periodetype
import java.time.LocalDate

data class PeriodebeløpJson(

    val utbetaltPerPeriode: Int,
    var periodetype: Periodetype,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
)

