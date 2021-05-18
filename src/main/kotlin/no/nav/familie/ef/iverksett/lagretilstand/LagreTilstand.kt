package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse

interface LagreTilstand {

    fun lagreTilkjentYtelseForUtbetaling(behandligId: String, tilkjentYtelseForUtbetaling: TilkjentYtelse)
    fun lagreOppdragResultat(behandligId: String, oppdragResultatJson: OppdragResultat)
    fun lagreJournalPostResultat(behandligId: String, journalPostResultatJson: JournalpostResultat)
    fun lagreDistribuerVedtaksbrevResultat(behandlingId: String, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat)
}