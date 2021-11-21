package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class SimuleringController(
        private val simuleringService: SimuleringService,
) {

    @PostMapping
    fun hentSimulering(@RequestBody simuleringDto: SimuleringDto): Ressurs<DetaljertSimuleringResultat> {
        val detaljertSimuleringResultat =
                simuleringService.hentSimulering(simuleringDto.toDomain())
        return Ressurs.success(detaljertSimuleringResultat)
    }

    @PostMapping("v2")
    fun hentSimuleringV2(@RequestBody simuleringDto: SimuleringDto): Ressurs<BeriketSimuleringsresultat> {
        val beriketSimuleringResultat =
                simuleringService.hentBeriketSimulering(simuleringDto.toDomain())
        return Ressurs.success(beriketSimuleringResultat)
    }

    @PostMapping("v2/korrigering")
    fun fiksSimuleringV2(@RequestBody beriketSimuleringsresultat: BeriketSimuleringsresultat): Ressurs<BeriketSimuleringsresultat> {

        if (beriketSimuleringsresultat.oppsummering.tidSimuleringHentet==null) {
            throw ApiFeil("Kan ikke korrigere når simuleringsoppsummeringen mangler tidSimuleringHentet", HttpStatus.BAD_REQUEST)
        }

        val oppsummering = lagSimuleringsoppsummering(
                beriketSimuleringsresultat.detaljer,
                beriketSimuleringsresultat.oppsummering.tidSimuleringHentet!!)
        
        return Ressurs.success(beriketSimuleringsresultat.copy(oppsummering = oppsummering))
    }
}