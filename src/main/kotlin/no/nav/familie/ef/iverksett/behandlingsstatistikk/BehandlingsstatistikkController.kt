package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk/behandlingsstatistikk"])
@ProtectedWithClaims(issuer = "azuread")
class BehandlingsstatistikkController(
    private val behandlingsstatistikkService: BehandlingsstatistikkService,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendBehandlingstatistikk(
        @RequestBody behandlingStatistikk: BehandlingsstatistikkDto,
    ) {
        if (!SikkerhetContext.kallKommerFraEfSak()) {
            throw ApiFeil("Kall kommer ikke fra ef-sak", HttpStatus.FORBIDDEN)
        }

        behandlingsstatistikkService.sendBehandlingstatistikk(behandlingStatistikk)
    }
}
