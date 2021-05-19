package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import java.util.*

interface HentTilstand {

    fun hentTilkjentYtelse(behandlingId: UUID) : TilkjentYtelse
}