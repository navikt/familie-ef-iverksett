package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import java.util.*

interface LagreTilstand {

    fun lagreTilkjentYtelseForUtbetaling(behandligId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse)
    fun oppdaterOppdragResultat(behandligId: UUID, oppdragResultatJson: OppdragResultat)
    fun oppdaterJournalpostResultat(behandligId: UUID, journalPostResultatJson: JournalpostResultat)
    fun oppdaterDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat)
}