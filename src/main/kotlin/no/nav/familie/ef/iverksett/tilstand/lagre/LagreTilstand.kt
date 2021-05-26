package no.nav.familie.ef.iverksett.tilstand.lagre

import no.nav.familie.ef.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import java.util.UUID

interface LagreTilstand {

    fun opprettTomtResultat(behandlingId: UUID)
    fun oppdaterTilkjentYtelseForUtbetaling(behandligId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse?)
    fun oppdaterOppdragResultat(behandligId: UUID, oppdragResultatJson: OppdragResultat)
    fun oppdaterJournalpostResultat(behandligId: UUID, journalPostResultatJson: JournalpostResultat)
    fun oppdaterDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat)
}