package no.nav.familie.ef.iverksett.simulering

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseMedMetaData

data class SimuleringDto(
        val nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        val forrigeTilkjentYtelse: TilkjentYtelse?
)