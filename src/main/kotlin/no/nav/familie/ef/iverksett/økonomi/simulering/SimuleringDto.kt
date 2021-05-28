package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData

data class SimuleringDto(
        val nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        val forrigeTilkjentYtelse: TilkjentYtelse?
)