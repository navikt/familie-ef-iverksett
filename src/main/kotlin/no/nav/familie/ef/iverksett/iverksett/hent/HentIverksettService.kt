package no.nav.familie.ef.iverksett.iverksett.hent

import no.nav.familie.ef.iverksett.iverksett.Brev
import no.nav.familie.ef.iverksett.iverksett.Iverksett
import no.nav.familie.ef.iverksett.iverksett.hent.HentIverksettJdbc
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HentIverksettService(val hentIverksettJdbc: HentIverksettJdbc) {

    fun hentIverksett(behandlingId: UUID): Iverksett {
        return hentIverksettJdbc.hent(behandlingId)
    }

    fun hentBrev(behandlingId: UUID): Brev {
        return hentIverksettJdbc.hentBrev(behandlingId)
    }

}