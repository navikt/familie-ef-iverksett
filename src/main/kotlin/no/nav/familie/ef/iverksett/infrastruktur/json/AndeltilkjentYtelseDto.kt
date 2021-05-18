package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.math.BigDecimal
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