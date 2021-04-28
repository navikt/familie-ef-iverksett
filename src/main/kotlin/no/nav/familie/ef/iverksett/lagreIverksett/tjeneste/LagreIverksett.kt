package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import arrow.core.Either

interface LagreIverksett {
    fun lagre(iverksettJson: String): Either<KunneIkkeLagreIverksett, Int>
}