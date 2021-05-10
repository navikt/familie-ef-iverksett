package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import java.util.*

class LagreIverksettService(val lagreIverksett: LagreIverksett) {

    fun lagreIverksett(behandlingsId: UUID, iverksett: Iverksett, brev: Brev) {
        return lagreIverksett.lagre(behandlingsId, iverksett, brev)
    }

}