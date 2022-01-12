package no.nav.familie.ef.iverksett.oppgave

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
        path = ["/api/iverksett/task/barnfylleraar"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class OpprettOppgaveForBarnTaskController(
        private val oppgaveForBarnService: OpprettOppgaveForBarnService
) {

    @PostMapping("/oppgaverforbarn")
    fun opprettTask(@RequestBody oppgaverForBarn: OppgaverForBarn): ResponseEntity<Unit> {
        oppgaveForBarnService.opprettTaskerForBarn(oppgaverForBarn.oppgaverForBarn)
        return ResponseEntity(HttpStatus.OK)
    }
}