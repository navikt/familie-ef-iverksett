package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import java.util.*

class LagreIverksettService(val lagreIverksett: LagreIverksett) {

    fun lagreIverksettJson(behandlingsId: UUID, iverksettJson: String, brev: List<Brev>) {
        return lagreIverksett.lagre(behandlingsId, iverksettJson, brev)
    }
}