package no.nav.familie.ef.iverksett.infrastruktur.configuration

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig(
    private val entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private fun lagMaskinTilMaskinRestKlient(scope: String): RestClient {
        val requestFactory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(Duration.ofSeconds(2))
                setReadTimeout(Duration.ofSeconds(60))
            }
        return entraIDRestClientFactory
            .lagMaskinTilMaskinRestKlient(scope)
            .mutate()
            .requestFactory(requestFactory)
            .build()
    }

    @Bean("oppdragRestClient")
    fun oppdragRestClient(
        @Value("\${FAMILIE_OPPDRAG_SCOPE}") scope: String,
    ): RestClient = lagMaskinTilMaskinRestKlient(scope)

    @Bean("integrasjonerRestClient")
    fun integrasjonerRestClient(
        @Value("\${FAMILIE_INTEGRASJONER_SCOPE}") scope: String,
    ): RestClient = lagMaskinTilMaskinRestKlient(scope)

    @Bean("tilbakekrevingRestClient")
    fun tilbakekrevingRestClient(
        @Value("\${FAMILIE_TILBAKE_SCOPE}") scope: String,
    ): RestClient = lagMaskinTilMaskinRestKlient(scope)
}
