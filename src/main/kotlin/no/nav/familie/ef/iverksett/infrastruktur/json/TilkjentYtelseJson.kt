package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseStatus
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.util.*

data class TilkjentYtelseJson(
    val id: UUID = UUID.randomUUID(),
    val behandlingId: UUID,
    val personident: String,
    val vedtaksdato: LocalDate? = null,
    val status: TilkjentYtelseStatus,
    val type: TilkjentYtelseType,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelseJson>
)

fun TilkjentYtelseJson.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        id = this.id,
        behandlingId = this.behandlingId,
        personident = this.personident,
        vedtaksdato = this.vedtaksdato,
        status = this.status,
        type = this.type,
        andelerTilkjentYtelse = this.andelerTilkjentYtelse.map { it.toDomain() })
}


