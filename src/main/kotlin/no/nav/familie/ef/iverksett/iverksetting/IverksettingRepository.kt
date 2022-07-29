package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IverksettingRepository : RepositoryInterface<Iverksett, UUID>, InsertUpdateRepository<Iverksett> {

    fun findByEksternId(eksternId: Long): Iverksett
}
