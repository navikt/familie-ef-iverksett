package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse

data class TilkjentYtelseDto(
    val andelerTilkjentYtelse: List<AndelTilkjentYtelseDto>
)

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        andelerTilkjentYtelse = this.andelerTilkjentYtelse.map { it.toDomain() })
}


