package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.kontrakter.ef.infotrygd.OpprettStartBehandlingHendelseDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/start-behandling")
@ProtectedWithClaims(issuer = "azuread")
class StartBehandlingController(
    private val infotrygdFeedClient: InfotrygdFeedClient,
) {
    @PostMapping
    fun startBehandling(
        @RequestBody request: OpprettStartBehandlingHendelseDto,
    ) {
        infotrygdFeedClient.opprettStartBehandlingHendelse(request)
    }
}
