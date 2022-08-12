package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto

fun AndelTilkjentYtelseDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.beløp,
        fraOgMed = this.periode.fomDato,
        tilOgMed = this.periode.tomDato,
        inntekt = this.inntekt,
        samordningsfradrag = this.samordningsfradrag,
        inntektsreduksjon = this.inntektsreduksjon,
        kildeBehandlingId = this.kildeBehandlingId
    )
}
