package no.nav.familie.ef.iverksett.brev.aktivitetsplikt

import no.nav.familie.ef.iverksett.brev.domain.AktivitetspliktBrev
import no.nav.familie.kontrakter.ef.felles.PeriodiskAktivitetspliktBrevDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.time.Year
import java.util.UUID

object AktivitetspliktInnhentingBrevUtil {
    fun opprettBrev(
        journalpostId: String? = null,
    ) = AktivitetspliktBrev(
        id = UUID.randomUUID(),
        personIdent = "12345678910",
        oppgaveId = 5L,
        eksternFagsakId = 6L,
        journalførendeEnhet = "enhet",
        fil = ByteArray(1),
        gjeldendeÅr = Year.of(2023),
        stønadType = StønadType.OVERGANGSSTØNAD,
        journalpostId = journalpostId,
    )

    fun brevDto(
        eksternFagsakId: Long,
        oppgaveId: Long,
    ) = PeriodiskAktivitetspliktBrevDto(
        "123".toByteArray(),
        oppgaveId,
        "ident",
        eksternFagsakId,
        "4489",
        Year.of(2023),
        StønadType.OVERGANGSSTØNAD,
    )
}
