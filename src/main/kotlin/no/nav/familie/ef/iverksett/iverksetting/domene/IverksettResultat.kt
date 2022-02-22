package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import java.time.LocalDateTime
import java.util.UUID

data class IverksettResultat(
        val behandlingId: UUID,
        val tilkjentYtelseForUtbetaling: TilkjentYtelse?,
        val oppdragResultat: OppdragResultat? = null,
        val journalpostResultat: Map<String, JournalpostResultat>? = null,
        val vedtaksbrevResultat: Map<String, DistribuerVedtaksbrevResultat>? = null,
        val tilbakekrevingResultat: TilbakekrevingResultat? = null
)

data class JournalpostResultat(
        val journalpostId: String,
        val journalført: LocalDateTime = LocalDateTime.now()
)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())

data class DistribuerVedtaksbrevResultat(val bestillingId: String?, val dato: LocalDateTime = LocalDateTime.now())

data class TilbakekrevingResultat(val opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest, val tilbakekrevingOppdatert: LocalDateTime = LocalDateTime.now())
