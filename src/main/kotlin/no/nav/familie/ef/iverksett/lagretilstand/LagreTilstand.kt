package no.nav.familie.ef.iverksett.lagretilstand

interface LagreTilstand {

    fun lagreJournalPostResultat(journalPostResultatJson: String)
    fun lagreOppdragResultat(oppdragResultatJson: String)
    fun lagreTilkjentYtelseForUtbetaling(tilkjentYtelseForUtbetaling: String)
}