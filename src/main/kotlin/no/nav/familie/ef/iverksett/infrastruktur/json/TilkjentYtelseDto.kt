package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse

@Deprecated("Overflødig. Bruk andelerTilkjentYtelse direkte i stedet")
data class TilkjentYtelseDto(
    val andelerTilkjentYtelse: List<AndelTilkjentYtelseDto>
)

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        andelerTilkjentYtelse = this.andelerTilkjentYtelse.map { it.toDomain() })
}


