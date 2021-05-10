package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.ef.felles.StønadType

data class TilkjentYtelseMedMetadataJson(
    val tilkjentYtelseJson: TilkjentYtelseJson,
    val saksbehandlerId: String,
    val eksternBehandlingId: Long,
    val stønadstype: StønadType,
    val eksternFagsakId: Long
)

fun TilkjentYtelseMedMetadataJson.toDomain(): TilkjentYtelseMedMetaData {
    return TilkjentYtelseMedMetaData(
        this.tilkjentYtelseJson.toDomain(),
        this.saksbehandlerId,
        this.eksternBehandlingId,
        this.stønadstype,
        this.eksternFagsakId
    )
}