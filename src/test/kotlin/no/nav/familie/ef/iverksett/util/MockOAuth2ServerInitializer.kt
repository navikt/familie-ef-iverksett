package no.nav.familie.ef.iverksett.util

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils

class MockOAuth2ServerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val issuerUrl = mockOAuth2Server.issuerUrl(ISSUER_ID).toString().trimEnd('/')
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "AZURE_OPENID_CONFIG_ISSUER=$issuerUrl",
            "AZURE_APP_CLIENT_ID=$AUDIENCE",
        )
    }

    companion object {
        const val ISSUER_ID = "azuread"
        const val AUDIENCE = "aud-localhost"

        val mockOAuth2Server: MockOAuth2Server by lazy {
            MockOAuth2Server().apply { start() }
        }
    }
}
