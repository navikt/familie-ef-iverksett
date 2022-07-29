package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import org.springframework.data.annotation.Id
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
    val vedtaksbrevResultat: VedtaksbrevResultatMap = VedtaksbrevResultatMap(),
    @Column("tilbakekrevingresultat")
    val tilbakekrevingResultat: TilbakekrevingResultat? = null
)

data class JournalpostResultatMap(val map: Map<String, JournalpostResultat> = emptyMap()) {

    operator fun plus(tillegg: Map<String, JournalpostResultat>): JournalpostResultatMap =
        JournalpostResultatMap(this.map + tillegg)

    fun isNotEmpty() = map.isNotEmpty()
}

data class VedtaksbrevResultatMap(val map: Map<String, DistribuerVedtaksbrevResultat> = emptyMap()) {

    operator fun plus(tillegg: Map<String, DistribuerVedtaksbrevResultat>): VedtaksbrevResultatMap =
        VedtaksbrevResultatMap(map + tillegg)

    fun isNotEmpty() = map.isNotEmpty()
}

data class JournalpostResultat(
    val journalpostId: String,
    val journalført: LocalDateTime = LocalDateTime.now()
)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())

data class DistribuerVedtaksbrevResultat(val bestillingId: String?, val dato: LocalDateTime = LocalDateTime.now())

data class TilbakekrevingResultat(
    val opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest,
    val tilbakekrevingOppdatert: LocalDateTime = LocalDateTime.now()
)
