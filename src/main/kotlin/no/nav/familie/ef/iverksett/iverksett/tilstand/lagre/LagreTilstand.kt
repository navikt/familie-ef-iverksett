package no.nav.familie.ef.iverksett.iverksett.tilstand.lagre

import no.nav.familie.ef.iverksett.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import java.util.UUID

interface LagreTilstand {

    fun opprettTomtResultat(behandlingId: UUID)
    fun oppdaterTilkjentYtelseForUtbetaling(behandligId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse?)
    fun oppdaterOppdragResultat(behandligId: UUID, oppdragResultatJson: OppdragResultat)
    fun oppdaterJournalpostResultat(behandligId: UUID, journalPostResultatJson: JournalpostResultat)
    fun oppdaterDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat)
}