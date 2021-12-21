package no.nav.familie.ef.iverksett.oppgave

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
        path = ["/api/iverksett/task/barnfylleraar"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class IverksettingController(
        private val opprettOppgaveForBarnTask: OpprettOppgaveForBarnTask
) {

    @GetMapping("/opprett")
    fun opprettTask(): ResponseEntity<Unit> {
        opprettOppgaveForBarnTask.opprettTaskForNesteUke()
        return ResponseEntity(HttpStatus.OK)
    }
}