package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import org.springframework.stereotype.Service
import java.util.*

@Service
class HentTilstandService(val hentTilstand: HentTilstand) {

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return hentTilstand.hentTilkjentYtelse(behandlingId)
    }

    fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat?{
        return hentTilstand.hentJournalpostResultat(behandlingId)
    }
}