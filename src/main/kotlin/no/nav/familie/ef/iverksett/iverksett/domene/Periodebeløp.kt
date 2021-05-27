package no.nav.familie.ef.iverksett.iverksett.domene

import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import java.time.LocalDate

data class Periodebeløp(

        val beløp: Int,
        var periodetype: Periodetype,
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
)