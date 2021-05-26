package no.nav.familie.ef.iverksett.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.iverksett.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HentTilstandService(val hentTilstand: HentTilstand) {

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return hentTilstand.hentTilkjentYtelse(behandlingId)
    }

    fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat? {
        return hentTilstand.hentJournalpostResultat(behandlingId)
    }

    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat? {
        return hentTilstand.hentIverksettResultat(behandlingId)
    }
}