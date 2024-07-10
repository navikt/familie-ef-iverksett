package no.nav.familie.ef.iverksett.featuretoggle

import no.nav.familie.unleash.UnleashService
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    val unleashService: UnleashService,
) {
    fun isEnabled(toggleId: String): Boolean = unleashService.isEnabled(toggleId)

    fun isEnabledMedFagsakId(
        toggleId: String,
        fagsakId: Long,
    ): Boolean = unleashService.isEnabled(toggleId, mapOf("fagsakId" to fagsakId.toString()))
}
