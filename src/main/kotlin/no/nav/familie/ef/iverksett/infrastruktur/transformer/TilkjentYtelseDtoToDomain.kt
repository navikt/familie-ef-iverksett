package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
            andelerTilkjentYtelse = this.andelerTilkjentYtelse.map { it.toDomain() })
}