package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class TilkjentYtelse(
    val id: UUID = UUID.randomUUID(),
    val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
    val status: TilkjentYtelseStatus = TilkjentYtelseStatus.IKKE_KLAR,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val sisteAndelIKjede: AndelTilkjentYtelse? = null,
    @Deprecated("Bruk startmåned", ReplaceWith("startmåned")) val startdato: LocalDate? = null,
    val startmåned: YearMonth = startdato?.let { YearMonth.from(startdato) }
        ?: error("Startdato eller startmåned må ha verdi")
) {

    fun toMedMetadata(
        saksbehandlerId: String,
        eksternBehandlingId: Long,
        stønadType: StønadType,
        eksternFagsakId: Long,
        personIdent: String,
        behandlingId: UUID,
        vedtaksdato: LocalDate
    ): TilkjentYtelseMedMetaData {
        return TilkjentYtelseMedMetaData(
            tilkjentYtelse = this,
            saksbehandlerId = saksbehandlerId,
            eksternBehandlingId = eksternBehandlingId,
            stønadstype = stønadType,
            eksternFagsakId = eksternFagsakId,
            personIdent = personIdent,
            behandlingId = behandlingId,
            vedtaksdato = vedtaksdato
        )
    }
}
