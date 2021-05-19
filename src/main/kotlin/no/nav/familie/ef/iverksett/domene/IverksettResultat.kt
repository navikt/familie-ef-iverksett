package no.nav.familie.ef.iverksett.domene

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import java.time.LocalDateTime

data class JournalpostResultat(
    val journalpostId: String,
    val journalført: LocalDateTime = LocalDateTime.now(),
    val bestillingId: String? = null
)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())

data class DistribuerVedtaksbrevResultat(val bestillingId: String, val dato: LocalDateTime = LocalDateTime.now())