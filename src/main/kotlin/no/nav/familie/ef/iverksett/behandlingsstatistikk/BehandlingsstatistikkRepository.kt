package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import java.util.UUID

interface BehandlingsstatistikkRepository : RepositoryInterface<Behandlingsstatistikk, UUID>,
                                            InsertUpdateRepository<Behandlingsstatistikk> {

    fun findByBehandlingIdAndHendelse(behandlingId: UUID, hendelse: Hendelse): Behandlingsstatistikk

}