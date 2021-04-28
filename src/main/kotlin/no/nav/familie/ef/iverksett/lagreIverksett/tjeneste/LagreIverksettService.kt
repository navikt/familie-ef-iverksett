package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import arrow.core.Either
import org.slf4j.LoggerFactory

class LagreIverksettService(val lagreBehandling: LagreIverksett) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun lagreIverksettJson(iverksettJson: String): Either<KunneIkkeLagreIverksett, Int> {
        return lagreBehandling.lagre(iverksettJson).mapLeft {
            return Either.Left(it)
        }
    }
}