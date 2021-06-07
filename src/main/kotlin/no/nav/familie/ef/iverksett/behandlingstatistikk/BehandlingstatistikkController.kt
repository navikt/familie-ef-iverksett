package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk/behandlingstatistikk"])
@ProtectedWithClaims(issuer = "azuread")
class BehandlingstatistikkController(val behandlingstatistikkService: BehandlingstatistikkService) {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendBehandlingstatistikk(@RequestBody behandlingStatistikk: BehandlingStatistikkDto) {
        behandlingstatistikkService.sendBehandlingstatistikk(behandlingStatistikk)
    }
}