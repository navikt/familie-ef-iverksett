package no.nav.familie.ef.iverksett.featuretoggle

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

enum class Toggle(val toggleId: String) {
    IVERKSETT_SIMULERINGSKONTROLL("familie.ef.iverksett.simuleringskontroll"),
    TEST_ENVIRONMENT("test.environment"),
    IVERKSETT_STOPP_IVERKSETTING("familie.ef.iverksett.stopp-iverksetting");
}

/**
 * TODO : Fjern når toggles er verifisert ok ift Unleash Next
 */
@RestController
@RequestMapping(path = ["/api/featuretoggle"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class FeatureToggleController(val featureToggleService: FeatureToggleService) {
    private val funksjonsbrytere = setOf(
        Toggle.IVERKSETT_STOPP_IVERKSETTING,
        Toggle.IVERKSETT_SIMULERINGSKONTROLL,
        Toggle.TEST_ENVIRONMENT
    )

    @GetMapping
    fun sjekkAlle(): Map<String, Boolean> {
        return funksjonsbrytere.associate { it.toggleId to featureToggleService.isEnabled(it.toggleId) }
    }
}
