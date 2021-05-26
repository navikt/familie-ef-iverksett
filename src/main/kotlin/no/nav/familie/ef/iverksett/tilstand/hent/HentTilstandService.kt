package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.iverksett.IverksettResultat
import no.nav.familie.ef.iverksett.iverksett.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.TilkjentYtelse
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