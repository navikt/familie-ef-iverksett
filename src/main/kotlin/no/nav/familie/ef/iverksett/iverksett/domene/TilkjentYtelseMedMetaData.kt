package no.nav.familie.ef.iverksett.iverksett.domene

import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelseMedMetaData(val tilkjentYtelse: TilkjentYtelse,
                                     val saksbehandlerId: String,
                                     val eksternBehandlingId: Long,
                                     val stønadstype: StønadType,
                                     val eksternFagsakId: Long,
                                     val personIdent: String,
                                     val behandlingId: UUID,
                                     val vedtaksdato: LocalDate
)


