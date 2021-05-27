package no.nav.familie.ef.iverksett.iverksett.tilstand.lagre

import no.nav.familie.ef.iverksett.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun opprettTomtResultat(behandlingId: UUID) {
        lagreTilstand.opprettTomtResultat(behandlingId)
    }

    fun lagreJournalPostResultat(behandlingId: UUID, journalPostResultatJson: JournalpostResultat) {
        lagreTilstand.oppdaterJournalpostResultat(behandlingId, journalPostResultatJson)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun lagreOppdragResultat(behandlingId: UUID, oppdragResultatJson: OppdragResultat) {
        lagreTilstand.oppdaterOppdragResultat(behandlingId, oppdragResultatJson)
    }

    fun lagreTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        lagreTilstand.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseForUtbetaling)
    }

    fun lagreDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat) {
        lagreTilstand.oppdaterDistribuerVedtaksbrevResultat(behandlingId, distribuerVedtaksbrevResultat)
    }
}