package no.nav.familie.ef.iverksett.oppgave.terminbarn

import no.nav.familie.kontrakter.ef.iverksett.OppgaverForBarnDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/oppgave-for-terminbarn"])
class OpprettOppgaverTerminbarnTaskController(
        private val oppgaveForBarnService: OpprettOppgaverTerminbarnService
) {

    @PostMapping("/opprett")
    fun opprettTask(@RequestBody oppgaverForBarn: OppgaverForBarnDto) {
        oppgaveForBarnService.opprettTaskerForTerminbarn(oppgaverForBarn.oppgaverForBarn)
    }
}