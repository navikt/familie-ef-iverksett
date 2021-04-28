package no.nav.familie.ef.iverksett.simulering

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class SimuleringController(
        private val simuleringService: SimuleringService,
) {
    @PostMapping()
    fun hentSimulering(simuleringDto: SimuleringDto): Ressurs<DetaljertSimuleringResultat> {
        val detaljertSimuleringResultat = simuleringService.hentSimulering(simuleringDto)
        return Ressurs.success(detaljertSimuleringResultat)
    }
}