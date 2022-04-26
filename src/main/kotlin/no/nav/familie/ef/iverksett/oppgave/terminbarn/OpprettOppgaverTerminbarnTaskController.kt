package no.nav.familie.ef.iverksett.oppgave.terminbarn

import no.nav.familie.kontrakter.ef.iverksett.OppgaverForBarnDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/oppgave-for-terminbarn"])
class OpprettOppgaverTerminbarnTaskController(
        private val oppgaverForTerminbarnService: OpprettOppgaverTerminbarnService
) {

    @PostMapping("/")
    fun opprettTask(@RequestBody oppgaverForBarn: OppgaverForBarnDto) {
        oppgaverForTerminbarnService.opprettTaskerForTerminbarn(oppgaverForBarn.oppgaverForBarn)
    }
}