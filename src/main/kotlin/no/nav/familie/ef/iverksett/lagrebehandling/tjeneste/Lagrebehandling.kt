package no.nav.familie.ef.iverksett.lagrebehandling.tjeneste

import arrow.core.Either

interface Lagrebehandling {
    fun lagre(iverksettJson: String): Either<KunneIkkeLagreBehandling, Int>
}