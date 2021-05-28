package no.nav.familie.ef.iverksett.iverksettingstatus.status.tilstand

import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class TilstandDbUtil(val tilstandJdbc: TilstandJdbc) {

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return tilstandJdbc.hentTilkjentYtelse(behandlingId)
    }

    fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat? {
        return tilstandJdbc.hentJournalpostResultat(behandlingId)
    }

    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat? {
        return tilstandJdbc.hentIverksettResultat(behandlingId)
    }

    fun opprettTomtResultat(behandlingId: UUID) {
        tilstandJdbc.opprettTomtResultat(behandlingId)
    }

    fun lagreJournalPostResultat(behandlingId: UUID, journalPostResultatJson: JournalpostResultat) {
        tilstandJdbc.oppdaterJournalpostResultat(behandlingId, journalPostResultatJson)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun lagreOppdragResultat(behandlingId: UUID, oppdragResultatJson: OppdragResultat) {
        tilstandJdbc.oppdaterOppdragResultat(behandlingId, oppdragResultatJson)
    }

    fun lagreTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        tilstandJdbc.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseForUtbetaling)
    }

    fun lagreDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat) {
        tilstandJdbc.oppdaterDistribuerVedtaksbrevResultat(behandlingId, distribuerVedtaksbrevResultat)
    }

}