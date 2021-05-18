package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service

@Service
class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun lagreJournalPostResultat(behandlingId: String, journalPostResultatJson: JournalpostResultat) {
        lagreTilstand.oppdaterJournalpostResultat(behandlingId, journalPostResultatJson)
    }
    fun lagreOppdragResultat(behandlingId: String, oppdragResultatJson: OppdragResultat) {
        lagreTilstand.oppdaterOppdragResultat(behandlingId, oppdragResultatJson)
    }
    fun lagreTilkjentYtelseForUtbetaling(behandlingId: String, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        lagreTilstand.lagreTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseForUtbetaling)
    }
    fun lagreDistribuerVedtaksbrevResultat(behandlingId: String, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat) {
        lagreTilstand.oppdaterDistribuerVedtaksbrevResultat(behandlingId, distribuerVedtaksbrevResultat)
    }
}