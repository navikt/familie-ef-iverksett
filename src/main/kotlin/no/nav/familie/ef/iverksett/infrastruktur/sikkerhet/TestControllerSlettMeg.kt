package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/test/mjau"])
class TestControllerSlettMeg {

    @GetMapping("/med-roller")
    @PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
    fun testMedRolle(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - du har en gyldig rolle"))

    @GetMapping("/med-beslutter")
    @PreAuthorize("hasRole('BESLUTTER')")
    fun testMedBeslutter(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - du har en gyldig rolle"))

    @GetMapping("/med-saksbehandler")
    @PreAuthorize("hasRole('SAKSBEHANDLER')")
    fun testMedSaksbehandler(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - du har en gyldig rolle"))

    @GetMapping("/med-application")
    @PreAuthorize("hasRole('APPLICATION')")
    fun testMedApplikasjon(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - du har en gyldig rolle"))

    @GetMapping("/uauthenticated")
    fun testUtenAuth(): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success("OK - ingen auth krevet"))

    @GetMapping("/fra-ef-sak-med-roller")
    @PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
    fun testFraEfSak(): ResponseEntity<Ressurs<String>> {
        SikkerhetContext.validerKallKommerFraEfSak()
        return ResponseEntity.ok(Ressurs.success("OK - kall kom fra ef-sak"))
    }

    @GetMapping("/hvem-er-du")
    @PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
    fun hvemErDu(): ResponseEntity<Ressurs<String>> {
        val jwt = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        val azpName = jwt.token.claims["azp_name"] as? String ?: "ukjent"
        val roles = jwt.authorities.map { it.authority }
        return ResponseEntity.ok(Ressurs.success("azp_name=$azpName, roller=$roles"))
    }
}
