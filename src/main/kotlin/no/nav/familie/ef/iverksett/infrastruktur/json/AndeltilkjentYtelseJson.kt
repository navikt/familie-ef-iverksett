package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.StønadsType

class AndeltilkjentYtelseJson(
    val periodebeløp: Periodebeløp,
    val personIdent: String,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
    val stønadsType: StønadsType
)

fun AndeltilkjentYtelseJson.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        this.periodebeløp,
        this.personIdent,
        this.periodeId,
        this.forrigePeriodeId,
        this.stønadsType
    )
}