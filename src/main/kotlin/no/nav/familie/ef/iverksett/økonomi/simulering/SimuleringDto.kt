package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import java.util.UUID

data class SimuleringDto(
        val nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        val forrigeBehandlingId: UUID
)