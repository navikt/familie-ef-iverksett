package no.nav.familie.ef.iverksett.iverksett

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import java.time.LocalDateTime
import java.util.*

data class IverksettResultat(
        val behandlingId: UUID,
        val tilkjentYtelseForUtbetaling: TilkjentYtelse?,
        val oppdragResultat: OppdragResultat? = null,
        val journalpostResultat: JournalpostResultat? = null,
        val vedtaksbrevResultat: DistribuerVedtaksbrevResultat? = null
)

data class JournalpostResultat(
    val journalpostId: String,
    val journalført: LocalDateTime = LocalDateTime.now()
)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())

data class DistribuerVedtaksbrevResultat(val bestillingId: String?, val dato: LocalDateTime = LocalDateTime.now())