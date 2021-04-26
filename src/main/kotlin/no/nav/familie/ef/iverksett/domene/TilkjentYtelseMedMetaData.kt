package no.nav.familie.ef.iverksett.domene

data class TilkjentYtelseMedMetaData(val tilkjentYtelse: TilkjentYtelse,
                                     val eksternBehandlingId: Long,
                                     val stønadstype: Stønadstype,
                                     val eksternFagsakId: Long)


