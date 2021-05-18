package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service

@Service
class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun lagreJournalPostResultat(behandligId: String, journalPostResultatJson: JournalpostResultat) {
        lagreTilstand.lagreJournalPostResultat(behandligId, journalPostResultatJson)
    }
    fun lagreOppdragResultat(behandligId: String, oppdragResultatJson: OppdragResultat) {
        lagreTilstand.lagreOppdragResultat(behandligId, oppdragResultatJson)
    }
    fun lagreTilkjentYtelseForUtbetaling(behandligId: String, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        lagreTilstand.lagreTilkjentYtelseForUtbetaling(behandligId, tilkjentYtelseForUtbetaling)
    }
}