package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import java.util.*

class HentTilstandService(val hentTilstand: HentTilstand) {

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse {
        return hentTilstand.hentTilkjentYtelse(behandlingId)
    }
}