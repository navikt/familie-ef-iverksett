package no.nav.familie.ef.iverksett.infrastruktur.configuration

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(
    private val entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    @Bean("oppdragRestClient")
    fun oppdragRestClient(
        @Value("\${FAMILIE_OPPDRAG_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    @Bean("integrasjonerRestClient")
    fun integrasjonerRestClient(
        @Value("\${FAMILIE_INTEGRASJONER_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    @Bean("tilbakekrevingRestClient")
    fun tilbakekrevingRestClient(
        @Value("\${FAMILIE_TILBAKE_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)
}
