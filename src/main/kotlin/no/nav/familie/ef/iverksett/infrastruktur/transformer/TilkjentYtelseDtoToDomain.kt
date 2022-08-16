package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseMedMetadata as TilkjentYtelseMedMetadataDto

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        andelerTilkjentYtelse = this.andelerTilkjentYtelse.map { it.toDomain() },
        startmåned = this.startmåned
    )
}

fun TilkjentYtelseMedMetadataDto.toDomain(): TilkjentYtelseMedMetaData {
    return TilkjentYtelseMedMetaData(
        tilkjentYtelse = this.tilkjentYtelse.toDomain(),
        saksbehandlerId = this.saksbehandlerId,
        eksternBehandlingId = this.eksternBehandlingId,
        stønadstype = this.stønadstype,
        eksternFagsakId = this.eksternFagsakId,
        personIdent = this.personIdent,
        behandlingId = this.behandlingId,
        vedtaksdato = this.vedtaksdato
    )
}
