package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
                simuleringService.hentSimulering(Simulering(nyTilkjentYtelseMedMetaData = simuleringDto.nyTilkjentYtelseMedMetaData.toDomain(),
                                                            forrigeBehandlingId = simuleringDto.forrigeBehandlingId))
        return Ressurs.success(detaljertSimuleringResultat)
    }
}