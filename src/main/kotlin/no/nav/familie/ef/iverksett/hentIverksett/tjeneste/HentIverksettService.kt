package no.nav.familie.ef.iverksett.hentIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Iverksett
import java.util.*

class HentIverksettService(val hentIverksett: HentIverksett) {

    fun hentIverksett(behandlingsId: String): Iverksett {
        return hentIverksett.hent(behandlingsId)
    }

}