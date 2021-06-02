package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk/behandlingstatistikk"])
@ProtectedWithClaims(issuer = "azuread")
class BehandlingstatistikkController {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendBehandlingstatistikk(@RequestBody behandlingStatistikk: BehandlingStatistikkDto): ResponseEntity<Unit> {
        //TODO: sende behandlingsstatistikk til DVH
        return ResponseEntity(HttpStatus.OK)
    }
}