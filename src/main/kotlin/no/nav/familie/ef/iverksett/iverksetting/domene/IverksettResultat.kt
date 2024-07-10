package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

data class IverksettResultat(
    @Id
    val behandlingId: UUID,
    @Column("tilkjentytelseforutbetaling")
    val tilkjentYtelseForUtbetaling: TilkjentYtelse? = null,
    @Column("oppdragresultat")
    val oppdragResultat: OppdragResultat? = null,
    @Column("journalpostresultat")
    val journalpostResultat: JournalpostResultatMap = JournalpostResultatMap(),
    @Column("vedtaksbrevresultat")
    val vedtaksbrevResultat: DistribuerBrevResultatMap = DistribuerBrevResultatMap(),
    @Column("tilbakekrevingresultat")
    val tilbakekrevingResultat: TilbakekrevingResultat? = null,
    @Version
    val versjon: Int = 0,
)

data class OppdragResultat(
    val oppdragStatus: OppdragStatus,
    val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now(),
)

data class TilbakekrevingResultat(
    val opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest,
    val tilbakekrevingOppdatert: LocalDateTime = LocalDateTime.now(),
)
