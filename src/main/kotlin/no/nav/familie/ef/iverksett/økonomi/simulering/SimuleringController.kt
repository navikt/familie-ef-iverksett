package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
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
