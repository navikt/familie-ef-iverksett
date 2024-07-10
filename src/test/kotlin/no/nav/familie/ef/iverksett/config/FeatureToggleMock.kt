package no.nav.familie.ef.iverksett.config

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("servertest")
@Configuration
class FeatureToggleMock {
    @Bean
    @Primary
    fun featureToggleService(): FeatureToggleService = mockFeatureToggleService()
}
