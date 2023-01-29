package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker
import no.nav.familie.kontrakter.felles.ef.StønadType

object FrittståendeBrevUtil {

    fun opprettFrittståendeBrev() = FrittståendeBrev(
        personIdent = "11",
        eksternFagsakId = 1L,
        journalførendeEnhet = "enhet",
        saksbehandlerIdent = "saksbehandlerIdent",
        stønadstype = StønadType.OVERGANGSSTØNAD,
        mottakere = Brevmottakere(
            listOf(
                no.nav.familie.ef.iverksett.brev.domain.Brevmottaker(
                    "11",
                    "navn1",
                    Brevmottaker.IdentType.PERSONIDENT,
                    Brevmottaker.MottakerRolle.BRUKER,
                ),
                no.nav.familie.ef.iverksett.brev.domain.Brevmottaker(
                    "22",
                    "navn2",
                    Brevmottaker.IdentType.PERSONIDENT,
                    Brevmottaker.MottakerRolle.BRUKER,
                ),
            ),
        ),
        fil = byteArrayOf(13),
        brevtype = FrittståendeBrevType.INFORMASJONSBREV,
    )
}
