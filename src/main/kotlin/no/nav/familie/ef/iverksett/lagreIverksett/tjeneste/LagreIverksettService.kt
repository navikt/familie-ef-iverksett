package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import arrow.core.Either
import no.nav.familie.ef.iverksett.domene.Brev
import org.slf4j.LoggerFactory

class LagreIverksettService(val lagreIverksett: LagreIverksett) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun lagreIverksettJson(iverksettJson: String, brev: List<Brev>): Either<KunneIkkeLagreIverksett, Int> {
        return lagreIverksett.lagre(iverksettJson, brev).mapLeft {
            logger.error("Kunne ikke lagre json av Iverksett")
            return Either.Left(it)
        }
    }
}