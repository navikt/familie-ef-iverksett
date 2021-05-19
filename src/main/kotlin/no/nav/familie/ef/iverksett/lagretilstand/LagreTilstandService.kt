package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service
import java.util.*

@Service
class LagreTilstandService(val lagreTilstand: LagreTilstand) {

    fun lagreJournalPostResultat(behandlingId: UUID, journalPostResultatJson: JournalpostResultat) {
        lagreTilstand.oppdaterJournalpostResultat(behandlingId, journalPostResultatJson)
    }
    fun lagreOppdragResultat(behandlingId: UUID, oppdragResultatJson: OppdragResultat) {
        lagreTilstand.oppdaterOppdragResultat(behandlingId, oppdragResultatJson)
    }
    fun lagreTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        lagreTilstand.lagreTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseForUtbetaling)
    }
    fun lagreDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat) {
        lagreTilstand.oppdaterDistribuerVedtaksbrevResultat(behandlingId, distribuerVedtaksbrevResultat)
    }
}