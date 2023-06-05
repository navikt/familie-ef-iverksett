package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import org.springframework.stereotype.Repository
import java.time.Year
import java.util.UUID

@Repository
interface KarakterutskriftBrevRepository :
    RepositoryInterface<KarakterutskriftBrev, UUID>, InsertUpdateRepository<KarakterutskriftBrev> {

    fun existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(eksternFagsakId: Long, oppgaveId: Long, gjeldendeÅr: Year): Boolean

    fun existsByEksternFagsakIdAndGjeldendeÅrAndBrevtype(
        eksternFagsakId: Long,
        gjeldendeÅr: Year,
        brevType: FrittståendeBrevType,
    ): Boolean
}
