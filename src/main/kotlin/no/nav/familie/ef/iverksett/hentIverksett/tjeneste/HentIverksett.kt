package no.nav.familie.ef.iverksett.hentIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett

interface HentIverksett {

    fun hent(behandlingsId: String): Iverksett
    fun hentBrev(behandlingsId: String): Brev

}