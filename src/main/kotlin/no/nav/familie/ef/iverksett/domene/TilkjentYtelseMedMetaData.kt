package no.nav.familie.ef.iverksett.domene

import no.nav.familie.kontrakter.ef.felles.StønadType

data class TilkjentYtelseMedMetaData(val tilkjentYtelse: TilkjentYtelse,
                                     val saksbehandlerId: String,
                                     val eksternBehandlingId: Long,
                                     val stønadstype: StønadType,
                                     val eksternFagsakId: Long)


