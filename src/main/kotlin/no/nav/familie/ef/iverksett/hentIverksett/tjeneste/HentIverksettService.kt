package no.nav.familie.ef.iverksett.hentIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett

class HentIverksettService(val hentIverksett: HentIverksett) {

    fun hentIverksett(behandlingsId: String): Iverksett {
        return hentIverksett.hent(behandlingsId)
    }

    fun hentBrev(behandlingsId: String): Brev {
        return hentIverksett.hentBrev(behandlingsId)
    }

}