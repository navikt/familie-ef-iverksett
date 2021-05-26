package no.nav.familie.ef.iverksett.tilstand.lagre

import no.nav.familie.ef.iverksett.iverksett.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksett.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.OppdragResultat
import no.nav.familie.ef.iverksett.iverksett.TilkjentYtelse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun opprettTomtResultat(behandlingId: UUID) {
        lagreTilstand.opprettTomtResultat(behandlingId)
    }

    fun lagreJournalPostResultat(behandlingId: UUID, journalPostResultatJson: JournalpostResultat) {
        lagreTilstand.oppdaterJournalpostResultat(behandlingId, journalPostResultatJson)
    }

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