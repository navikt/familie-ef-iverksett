package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseStatus
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseType
import java.time.LocalDate
import java.util.*

data class TilkjentYtelseDto(
    val id: UUID = UUID.randomUUID(),
    val status: TilkjentYtelseStatus,
    val type: TilkjentYtelseType,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelseDto>
)

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        id = this.id,
        status = this.status,
        type = this.type,
        andelerTilkjentYtelse = this.andelerTilkjentYtelse.map { it.toDomain() })
}


