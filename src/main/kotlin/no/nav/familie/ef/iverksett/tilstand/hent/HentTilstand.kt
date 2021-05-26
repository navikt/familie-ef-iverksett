package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.domene.IverksettResultat
import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import java.util.UUID

interface HentTilstand {

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse?
    fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat?
    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat?
}