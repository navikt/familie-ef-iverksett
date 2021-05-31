package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto

fun AndelTilkjentYtelseDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
            periodebeløp = this.periodebeløp.toDomain(),
            kildeBehandlingId = this.kildeBehandlingId
    )
}