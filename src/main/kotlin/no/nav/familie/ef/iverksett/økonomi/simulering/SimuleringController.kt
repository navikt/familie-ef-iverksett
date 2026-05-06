package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/simulering")
@PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
@Validated
class SimuleringController(
    private val simuleringService: SimuleringService,
) {
    @PostMapping("v2")
    fun hentSimuleringV2(
        @RequestBody simuleringDto: SimuleringDto,
    ): Ressurs<BeriketSimuleringsresultat> {
        SikkerhetContext.validerKallKommerFraEfSak()
        val beriketSimuleringResultat = simuleringService.hentBeriketSimulering(simuleringDto.toDomain())
        return Ressurs.success(beriketSimuleringResultat)
    }
}
