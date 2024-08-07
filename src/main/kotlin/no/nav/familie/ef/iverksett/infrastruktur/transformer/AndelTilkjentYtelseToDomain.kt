package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto

fun AndelTilkjentYtelseDto.toDomain(): AndelTilkjentYtelse =
    AndelTilkjentYtelse(
        beløp = this.beløp,
        periode = this.periode,
        inntekt = this.inntekt,
        samordningsfradrag = this.samordningsfradrag,
        inntektsreduksjon = this.inntektsreduksjon,
        kildeBehandlingId = this.kildeBehandlingId,
    )
