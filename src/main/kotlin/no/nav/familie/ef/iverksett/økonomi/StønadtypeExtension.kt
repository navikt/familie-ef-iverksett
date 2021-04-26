package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.domene.Stønadstype

fun Stønadstype.tilKlassifisering() = when (this) {
    Stønadstype.OVERGANGSSTØNAD -> "EFOG"
    Stønadstype.BARNETILSYN -> "EFBT"
    Stønadstype.SKOLEPENGER -> "EFSP"
}
