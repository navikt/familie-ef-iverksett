package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IverksettingRepository : RepositoryInterface<Iverksett, UUID>, InsertUpdateRepository<Iverksett> {
    @Query("SELECT behandling_id from iverksett")
    fun finnAlleIder(): List<UUID>

    fun findByEksternId(eksternId: Long): Iverksett
}
