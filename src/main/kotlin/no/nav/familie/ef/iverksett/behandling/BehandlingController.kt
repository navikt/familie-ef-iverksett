package no.nav.familie.ef.iverksett.behandling

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping(path = ["/api/behandling"])
@ProtectedWithClaims(issuer = "azuread")
class BehandlingController(private val behandlingService: BehandlingService) {

    @GetMapping
    fun hentBeregnetInntektForBehandling(@PathVariable eksternId: Long, dato: LocalDate = LocalDate.now()): Ressurs<Int> {
        val beregnetInntekt = behandlingService.hentBeregnetInntektForBehandlingOgDato(eksternId, dato)
                              ?: return Ressurs.funksjonellFeil("Fant ingen registrert inntekt for behandling $eksternId og gitt dato $dato")
        return Ressurs.success(beregnetInntekt)
    }
}