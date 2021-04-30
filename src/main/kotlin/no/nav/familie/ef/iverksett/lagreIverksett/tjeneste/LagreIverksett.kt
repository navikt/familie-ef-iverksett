package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import java.util.*

interface LagreIverksett {

    fun lagre(behandlingsId: UUID, iverksettJson: String, brev: List<Brev>)
}