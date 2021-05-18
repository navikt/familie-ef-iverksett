package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur.LagreIverksettJdbc
import org.springframework.stereotype.Service
import java.util.*

@Service
class LagreIverksettService(val lagreIverksettJdbc: LagreIverksettJdbc) {

    fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        return lagreIverksettJdbc.lagre(behandlingId, iverksett, brev)
    }

}