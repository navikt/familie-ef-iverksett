package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import java.util.*

interface LagreIverksett {

    fun lagre(behandlingsId: UUID, iverksett: Iverksett, brev: Brev)
}