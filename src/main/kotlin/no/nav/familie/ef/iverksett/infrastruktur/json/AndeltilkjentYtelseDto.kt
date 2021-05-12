package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.math.BigDecimal
import java.util.*

class AndelTilkjentYtelseDto(
    val periodebeløp: Periodebeløp,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
    val kildeBehandlingId: UUID? = null
)

fun AndelTilkjentYtelseDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        this.periodebeløp,
        this.periodeId,
        this.forrigePeriodeId,
        this.kildeBehandlingId
    )
}