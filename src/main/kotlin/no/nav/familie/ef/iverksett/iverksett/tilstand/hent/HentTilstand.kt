package no.nav.familie.ef.iverksett.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.iverksett.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import java.util.UUID

interface HentTilstand {

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse?
    fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat?
    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat?
}