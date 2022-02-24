package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelse(
        val id: UUID = UUID.randomUUID(),
        val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
        val status: TilkjentYtelseStatus = TilkjentYtelseStatus.IKKE_KLAR,
        val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
        val sisteAndelIKjede: AndelTilkjentYtelse? = null,
        // todo kunde vurdert å ha med sist opphørtFomDato eller noe slik for hvilket opphørsdato man skal sende i neste (som då kan sjekkes hvis man har 0 andeler i tidligere TY)
        val startdato: LocalDate?) {


    fun toMedMetadata(
            saksbehandlerId: String,
            eksternBehandlingId: Long,
            stønadType: StønadType,
            eksternFagsakId: Long,
            personIdent: String,
            behandlingId: UUID,
            vedtaksdato: LocalDate
    ): TilkjentYtelseMedMetaData {
        return TilkjentYtelseMedMetaData(tilkjentYtelse = this,
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
