package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto

fun SimuleringDto.toDomain(): Simulering {
    return Simulering(nyTilkjentYtelseMedMetaData = this.nyTilkjentYtelseMedMetaData.toDomain(),
                      forrigeBehandlingId = this.forrigeBehandlingId
    )
}

