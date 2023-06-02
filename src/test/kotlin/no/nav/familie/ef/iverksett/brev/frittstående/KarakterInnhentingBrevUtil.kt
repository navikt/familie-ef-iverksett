package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.time.Year
import java.util.UUID

object KarakterInnhentingBrevUtil {

    fun opprettBrev(brevType: FrittståendeBrevType, journalpostId: String? = null) = KarakterutskriftBrev(
        id = UUID.randomUUID(),
        personIdent = "12345678910",
        oppgaveId = 5L,
        eksternFagsakId = 6L,
        journalførendeEnhet = "enhet",
        fil = ByteArray(1),
        brevtype = brevType,
        gjeldendeÅr = Year.of(2023),
        stønadType = StønadType.OVERGANGSSTØNAD,
        journalpostId = journalpostId,
    )
}
