package no.nav.familie.ef.iverksett.lagretilstand

interface LagreTilstand {

    fun lagreTilkjentYtelseForUtbetaling(behandligId: String, tilkjentYtelseForUtbetaling: String)
    fun lagreOppdragResultat(behandligId: String, oppdragResultatJson: String)
    fun lagreJournalPostResultat(behandligId: String, journalPostResultatJson: String)

}