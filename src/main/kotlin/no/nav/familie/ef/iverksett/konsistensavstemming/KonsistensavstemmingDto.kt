package no.nav.familie.ef.iverksett.konsistensavstemming

import no.nav.familie.ef.iverksett.infrastruktur.json.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.util.*

data class KonsistensavstemmingDto(
        val tilkjenteYtelser: List<KonsistensavstemmingTilkjentYtelseDto>
)

data class KonsistensavstemmingTilkjentYtelseDto(
        val behandlingId: UUID,
        val eksternId: Long,
        val stønadType: StønadType,
        val eksternFagsakId: Long,
        val personIdent: String,
        val vedtaksdato: LocalDate,
        val andelerTilkjentYtelse: List<AndelTilkjentYtelseDto>
)