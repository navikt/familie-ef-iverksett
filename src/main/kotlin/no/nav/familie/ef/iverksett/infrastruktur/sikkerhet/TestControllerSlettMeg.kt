package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/test/mjau"])
class TestControllerSlettMeg {

    @GetMapping("/med-roller")
    @PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
    fun testMedRolle(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - du har en gyldig rolle"))

    @GetMapping("/uauthenticated")
    fun testUtenAuth(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - ingen auth krevet"))

    @GetMapping("/fra-ef-sak-med-roller")
    @PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
    fun testFraEfSak(): ResponseEntity<Ressurs<String>> {
        SikkerhetContext.validerKallKommerFraEfSak()
        return ResponseEntity.ok(Ressurs.success("OK - kall kom fra ef-sak"))
    }
}
