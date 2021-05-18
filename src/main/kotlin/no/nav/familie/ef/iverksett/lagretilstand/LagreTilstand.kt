package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse

interface LagreTilstand {

    fun lagreTilkjentYtelseForUtbetaling(behandligId: String, tilkjentYtelseForUtbetaling: TilkjentYtelse)
    fun oppdaterOppdragResultat(behandligId: String, oppdragResultatJson: OppdragResultat)
    fun oppdaterJournalpostResultat(behandligId: String, journalPostResultatJson: JournalpostResultat)
    fun oppdaterDistribuerVedtaksbrevResultat(behandlingId: String, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat)
}