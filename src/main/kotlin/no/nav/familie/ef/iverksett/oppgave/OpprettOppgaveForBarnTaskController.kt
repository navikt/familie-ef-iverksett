package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.domene.Task
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Properties

@RestController
@RequestMapping(
        path = ["/api/iverksett/task/barnfylleraar"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class OpprettOppgaveForBarnTaskController(
        private val oppgaveForBarnTask: OpprettOppgaveForBarnTask
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/oppgaverforbarn")
    fun opprettTask(@RequestBody oppgaverForBarn: OppgaverForBarn): ResponseEntity<Unit> {
        oppgaverForBarn.oppgaverForBarn.forEach {
            try {
                oppgaveForBarnTask.doTask(Task(OpprettOppgaveForBarnTask.TYPE,
                                               objectMapper.writeValueAsString(it),
                                               Properties()))
            } catch (ex: Exception) {
                logger.error("Kunne ikke opprette task for barn som fyller år med OppgaveForBarn=$it")
            }
        }
        return ResponseEntity(HttpStatus.OK)
    }
}