package no.nav.familie.ef.iverksett.simulering

import no.nav.familie.ef.iverksett.iverksett.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksett.TilkjentYtelseMedMetaData

data class SimuleringDto(
        val nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        val forrigeTilkjentYtelse: TilkjentYtelse?
)