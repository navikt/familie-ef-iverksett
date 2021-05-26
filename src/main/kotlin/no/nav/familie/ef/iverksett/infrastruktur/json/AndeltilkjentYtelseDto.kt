package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.iverksett.AndelTilkjentYtelse
import java.util.*

class AndelTilkjentYtelseDto(
    val periodebeløp: PeriodebeløpDto,
    val kildeBehandlingId: UUID? = null
)

fun AndelTilkjentYtelseDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        periodebeløp = this.periodebeløp.toDomain(),
        kildeBehandlingId = this.kildeBehandlingId
    )
}