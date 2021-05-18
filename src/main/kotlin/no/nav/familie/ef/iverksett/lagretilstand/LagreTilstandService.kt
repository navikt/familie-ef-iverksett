package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service

@Service
class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun lagreJournalPostResultat(behandlingId: String, journalPostResultatJson: JournalpostResultat) {
        lagreTilstand.lagreJournalPostResultat(behandlingId, journalPostResultatJson)
    }
    fun lagreOppdragResultat(behandlingId: String, oppdragResultatJson: OppdragResultat) {
        lagreTilstand.lagreOppdragResultat(behandlingId, oppdragResultatJson)
    }
    fun lagreTilkjentYtelseForUtbetaling(behandlingId: String, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        lagreTilstand.lagreTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseForUtbetaling)
    }
    fun lagreDistribuerVedtaksbrevResultat(behandlingId: String, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat) {
        lagreTilstand.lagreDistribuerVedtaksbrevResultat(behandlingId, distribuerVedtaksbrevResultat)
    }
}