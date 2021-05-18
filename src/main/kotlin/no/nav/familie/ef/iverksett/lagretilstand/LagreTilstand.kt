package no.nav.familie.ef.iverksett.lagretilstand

interface LagreTilstand {

    fun lagreJournalPostResultat(behandligId: String, journalPostResultatJson: String)
    fun lagreOppdragResultat(behandligId: String, oppdragResultatJson: String)
    fun lagreTilkjentYtelseForUtbetaling(behandligId: String, tilkjentYtelseForUtbetaling: String)
}