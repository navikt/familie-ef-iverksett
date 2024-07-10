package no.nav.familie.ef.iverksett.brev.aktivitetsplikt

import no.nav.familie.ef.iverksett.brev.domain.AktivitetspliktBrev
import no.nav.familie.ef.iverksett.repository.InsertUpdateRepository
import no.nav.familie.ef.iverksett.repository.RepositoryInterface
import org.springframework.stereotype.Repository
import java.time.Year
import java.util.UUID

@Repository
interface AktivitetspliktBrevRepository :
    RepositoryInterface<AktivitetspliktBrev, UUID>,
    InsertUpdateRepository<AktivitetspliktBrev> {
    fun existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(
        eksternFagsakId: Long,
        oppgaveId: Long,
        gjeldendeÅr: Year,
    ): Boolean

    fun existsByEksternFagsakIdAndGjeldendeÅr(
        eksternFagsakId: Long,
        gjeldendeÅr: Year,
    ): Boolean
}
