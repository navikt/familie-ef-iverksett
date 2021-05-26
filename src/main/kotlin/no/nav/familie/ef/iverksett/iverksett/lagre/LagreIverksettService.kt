package no.nav.familie.ef.iverksett.iverksett.lagre

import no.nav.familie.ef.iverksett.iverksett.Brev
import no.nav.familie.ef.iverksett.iverksett.Iverksett
import no.nav.familie.ef.iverksett.iverksett.lagre.LagreIverksettJdbc
import org.springframework.stereotype.Service
import java.util.*

@Service
class LagreIverksettService(val lagreIverksettJdbc: LagreIverksettJdbc) {

    fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        return lagreIverksettJdbc.lagre(behandlingId, iverksett, brev)
    }

}