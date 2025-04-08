package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.kontrakter.ef.infotrygd.OpprettStartBehandlingHendelseDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/start-behandling")
@ProtectedWithClaims(issuer = "azuread")
class StartBehandlingController {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @Deprecated("Ikke lenger i bruk etter at rutine i infotrygd ble skrudd av")
    fun startBehandling(
        @RequestBody request: OpprettStartBehandlingHendelseDto,
    ) {
        logger.warn("Deprecated - Ikke lenger i bruk etter at rutine i infotrygd ble skrudd av")
    }
}
