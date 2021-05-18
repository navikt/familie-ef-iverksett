package no.nav.familie.ef.iverksett.lagretilstand

class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun lagreJournalPostResultat(journalPostResultatJson: String) {
        lagreTilstand.lagreJournalPostResultat(journalPostResultatJson)
    }
    fun lagreOppdragResultat(oppdragResultatJson: String) {
        lagreTilstand.lagreOppdragResultat(oppdragResultatJson)
    }
    fun lagreTilkjentYtelseForUtbetaling(tilkjentYtelseForUtbetaling: String) {
        lagreTilstand.lagreTilkjentYtelseForUtbetaling(tilkjentYtelseForUtbetaling)
    }
}