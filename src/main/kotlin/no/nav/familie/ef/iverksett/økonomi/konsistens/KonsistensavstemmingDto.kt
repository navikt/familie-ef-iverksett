package no.nav.familie.ef.iverksett.økonomi.konsistens

import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.PeriodebeløpDto
import java.util.UUID

//TODO bytt ut mot DTO i kontrakter
data class KonsistensavstemmingDto(
        val stønadType: StønadType,
        val tilkjenteYtelser: List<KonsistensavstemmingTilkjentYtelseDto>
)

data class KonsistensavstemmingTilkjentYtelseDto(
        val behandlingId: UUID,
        val eksternBehandlingId: Long,
        val eksternFagsakId: Long,
        val personIdent: String,
        val andelerTilkjentYtelse: List<PeriodebeløpDto>
)