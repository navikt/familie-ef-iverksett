package no.nav.familie.ef.iverksett.lagretilstand

class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun lagreJournalPostResultat(behandligId: String, journalPostResultatJson: String) {
        lagreTilstand.lagreJournalPostResultat(behandligId, journalPostResultatJson)
    }
    fun lagreOppdragResultat(behandligId: String, oppdragResultatJson: String) {
        lagreTilstand.lagreOppdragResultat(behandligId, oppdragResultatJson)
    }
    fun lagreTilkjentYtelseForUtbetaling(behandligId: String, tilkjentYtelseForUtbetaling: String) {
        lagreTilstand.lagreTilkjentYtelseForUtbetaling(behandligId, tilkjentYtelseForUtbetaling)
    }
}