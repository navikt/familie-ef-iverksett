package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import arrow.core.Either
import no.nav.familie.ef.iverksett.domene.Brev

interface LagreIverksett {
    fun lagre(iverksettJson: String, brev: List<Brev>): Either<KunneIkkeLagreIverksett, Int>
}