package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus

data class OppdragStatusMedMelding(
        val status: OppdragStatus,
        val melding: String
)