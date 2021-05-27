package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksett.domene.Periodebeløp
import no.nav.familie.kontrakter.ef.iverksett.PeriodebeløpDto

fun PeriodebeløpDto.toDomain(): Periodebeløp {
    return Periodebeløp(
            this.beløp,
            this.periodetype,
            this.fraOgMed,
            this.tilOgMed
    )
}