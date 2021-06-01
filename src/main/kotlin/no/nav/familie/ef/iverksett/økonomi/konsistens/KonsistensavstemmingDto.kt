package no.nav.familie.ef.iverksett.økonomi.konsistens

import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.PeriodebeløpDto
import java.util.UUID

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