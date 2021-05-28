package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import org.springframework.stereotype.Service
import java.util.*

@Service
class IverksettDbUtil(val iverksettJdbc: IverksettJdbc) {

    fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        return iverksettJdbc.lagre(behandlingId, iverksett, brev)
    }

    fun hentIverksett(behandlingId: UUID): Iverksett {
        return iverksettJdbc.hent(behandlingId)
    }

    fun hentBrev(behandlingId: UUID): Brev {
        return iverksettJdbc.hentBrev(behandlingId)
    }
}