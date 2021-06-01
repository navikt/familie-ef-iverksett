package no.nav.familie.ef.iverksett.økonomi.konsistens

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/konsistensavstemming")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class KonsistensavstemmingController(
        private val konsistensavstemmingService: KonsistensavstemmingService,
) {

    @PostMapping
    fun startKonsistensavstemming(@RequestBody konsistensavstemmingDto: KonsistensavstemmingDto) {
        konsistensavstemmingService.sendKonsistensavstemming(konsistensavstemmingDto)
    }
}