package no.nav.familie.ef.iverksett.infrastruktur

import no.nav.familie.ef.iverksett.infrastruktur.json.VedtakJSON
import no.nav.familie.ef.iverksett.infrastruktur.json.transform
import no.nav.familie.ef.iverksett.mottak.tjeneste.MottakService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"])
@ProtectedWithClaims(issuer = "azuread")
class IverksettController(val mottakService: MottakService) {

    @PostMapping(path = ["/test"])
    fun test(@RequestBody vedtakJSON: VedtakJSON): ResponseEntity<String> {
        return ResponseEntity<String>(mottakService.test(vedtakJSON.transform()), HttpStatus.OK)
    }

}

