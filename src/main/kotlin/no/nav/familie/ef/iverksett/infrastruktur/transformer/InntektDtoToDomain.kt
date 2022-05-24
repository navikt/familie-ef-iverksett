package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.Inntekt
import no.nav.familie.kontrakter.ef.iverksett.InntektDto

fun InntektDto.toDomain22(): Inntekt {
    return Inntekt(beløp = this.beløp,
                   samordningsfradrag = this.samordningsfradrag,
                   fraOgMed = this.fraOgMed,
                   tilOgMed = this.tilOgMed)
}