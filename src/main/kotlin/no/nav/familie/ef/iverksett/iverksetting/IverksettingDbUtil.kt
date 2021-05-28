package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IverksettingDbUtil(val iverksettingJdbc: IverksettingJdbc) {

    fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        return iverksettingJdbc.lagre(behandlingId, iverksett, brev)
    }

    fun hentIverksett(behandlingId: UUID): Iverksett {
        return iverksettingJdbc.hent(behandlingId)
    }

    fun hentBrev(behandlingId: UUID): Brev {
        return iverksettingJdbc.hentBrev(behandlingId)
    }
}