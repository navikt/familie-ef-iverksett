package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.util.*

class AndelTilkjentYtelseJson(
    val periodebeløp: Periodebeløp,
    val personIdent: String,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
    val stønadsType: StønadType? = null,
    val kildeBehandlingId: UUID? = null
)

fun AndelTilkjentYtelseJson.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        this.periodebeløp,
        this.personIdent,
        this.periodeId,
        this.forrigePeriodeId,
        this.stønadsType,
        this.kildeBehandlingId
    )
}