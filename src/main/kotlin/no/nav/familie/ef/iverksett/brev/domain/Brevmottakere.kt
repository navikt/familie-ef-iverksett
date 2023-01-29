package no.nav.familie.ef.iverksett.brev.domain

import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.IdentType
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle
import no.nav.familie.kontrakter.felles.BrukerIdType

data class Brevmottakere(val mottakere: List<Brevmottaker>)
data class Brevmottaker(
    val ident: String,
    val navn: String,
    val identType: IdentType,
    val mottakerRolle: MottakerRolle,
)

fun IdentType.tilIdType(): BrukerIdType =
    when (this) {
        IdentType.ORGANISASJONSNUMMER -> BrukerIdType.ORGNR
        IdentType.PERSONIDENT -> BrukerIdType.FNR
    }
