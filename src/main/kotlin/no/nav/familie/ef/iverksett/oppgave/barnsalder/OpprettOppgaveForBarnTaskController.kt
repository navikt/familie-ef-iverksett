package no.nav.familie.ef.iverksett.oppgave.barnsalder

import no.nav.familie.kontrakter.ef.iverksett.OppgaverForBarnDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/api/oppgave-for-barns-alder"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class OpprettOppgaveForBarnTaskController(
    private val oppgaveForBarnService: OpprettOppgaverForBarnService
) {

    @PostMapping("/opprett")
    fun opprettTask(@RequestBody oppgaverForBarn: OppgaverForBarnDto) {
        oppgaveForBarnService.opprettTaskerForBarn(oppgaverForBarn.oppgaverForBarn)
    }
}
