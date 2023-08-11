package no.nav.familie.ef.iverksett.økonomi.simulering.kontroll

import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SimuleringskontrollRepository :
    RepositoryInterface<Simuleringskontroll, UUID>,
    InsertUpdateRepository<Simuleringskontroll>

data class Simuleringskontroll(
    @Id
    val behandlingId: UUID,
    @Column("input")
    val input: SimuleringskontrollInput,
    @Column("resultat")
    val resultat: SimuleringskontrollResultat,
)

data class SimuleringskontrollInput(
    val simulering: Simulering,
    val resultat: BeriketSimuleringsresultat,
)

data class SimuleringskontrollResultat(
    val resultat: BeriketSimuleringsresultat,
)
