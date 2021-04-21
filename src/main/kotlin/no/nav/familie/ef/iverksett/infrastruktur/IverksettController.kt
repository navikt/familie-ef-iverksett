package no.nav.familie.ef.iverksett.infrastruktur

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.iverksett.tjeneste.IverksettService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"])
@ProtectedWithClaims(issuer = "azuread")
class IverksettController(val iverksettService: IverksettService) {

    @PostMapping(path = ["/iverksett"])
    fun iverksett(@RequestBody vedtakJSON: IverksettJson): ResponseEntity<String> {
        return ResponseEntity("Iverksett", HttpStatus.OK)
    }

}

