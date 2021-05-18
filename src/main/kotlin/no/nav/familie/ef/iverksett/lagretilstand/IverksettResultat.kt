package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import java.time.LocalDateTime

data class IverksettResultat(
    val behandlingId: String,
    val tilkjentYtelseForUtbetaling: TilkjentYtelse,
    val oppdragResultat: OppdragResultat? = null,
    val journalpostResultat: JournalpostResultat? = null
)
data class JournalpostResultat(val journalpostId: String,
                               val journalført: LocalDateTime = LocalDateTime.now(),
                               val bestillingId: String? = null)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())