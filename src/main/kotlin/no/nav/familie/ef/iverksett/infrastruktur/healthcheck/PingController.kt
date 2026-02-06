package no.nav.familie.ef.iverksett.infrastruktur.healthcheck

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class PingController {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK"))
}
