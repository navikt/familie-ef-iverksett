package no.nav.familie.ef.iverksett.domene

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.util.*

data class TilkjentYtelse(
                          val id: UUID = UUID.randomUUID(),
                          val behandlingId: UUID,
                          val personident: String,
                          val stønadFom: LocalDate? = null,
                          val stønadTom: LocalDate? = null,
                          val opphørFom: LocalDate? = null,
                          val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
                          val vedtaksdato: LocalDate? = null,
                          val status: TilkjentYtelseStatus = TilkjentYtelseStatus.IKKE_KLAR,
                          val type: TilkjentYtelseType = TilkjentYtelseType.FØRSTEGANGSBEHANDLING,
                          val andelerTilkjentYtelse: List<AndelTilkjentYtelse>) {

}

enum class TilkjentYtelseStatus {
    IKKE_KLAR,
    OPPRETTET,
    SENDT_TIL_IVERKSETTING,
    AKTIV,
    AVSLUTTET
}

enum class TilkjentYtelseType {
    FØRSTEGANGSBEHANDLING,
    OPPHØR,
    ENDRING
}

