package no.nav.familie.ef.iverksett.util

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService

fun mockFeatureToggleService(enabled: Boolean = true): FeatureToggleService {
    val mockk = mockk<FeatureToggleService>()
    every { mockk.isEnabled(any()) } returns enabled
    every { mockk.isEnabled("familie.ef.iverksett.stopp-iverksetting") } returns false
    return mockk
}
