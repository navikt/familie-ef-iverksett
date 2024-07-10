package no.nav.familie.ef.iverksett.iverksetting.tilstand

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IverksettResultatRepository :
    RepositoryInterface<IverksettResultat, UUID>,
    InsertUpdateRepository<IverksettResultat> {
    @Query("SELECT behandling_id from iverksett_resultat")
    fun finnAlleIder(): List<UUID>
}
