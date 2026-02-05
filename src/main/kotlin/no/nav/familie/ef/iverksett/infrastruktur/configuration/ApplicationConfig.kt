package no.nav.familie.ef.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider
import no.nav.familie.http.client.RetryOAuth2HttpClient
import no.nav.familie.http.config.RestTemplateAzure
import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ConfigurationPropertiesScan("no.nav.familie.ef.iverksett")
@ComponentScan(
    "no.nav.familie.ef.iverksett",
    "no.nav.familie.prosessering",
    "no.nav.familie.sikkerhet",
    "no.nav.familie.unleash",
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [MappingJackson2XmlHttpMessageConverter::class]),
    ],
)
@EnableJwtTokenValidation(ignore = ["org.springframework"])
@Import(
    RestTemplateAzure::class,
    KafkaErrorHandler::class,
)
@EnableOAuth2Client(cacheEnabled = true)
@EnableScheduling
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule.Builder().build()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> =
        FilterRegistrationBean(LogFilter(systemtype = NavSystemtype.NAV_INTEGRASJON)).apply {
            logger.info("Registering LogFilter filter")
            order = 1
        }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> =
        FilterRegistrationBean(RequestTimeFilter()).apply {
            logger.info("Registering RequestTimeFilter filter")
            order = 2
        }

    @Bean
    @Primary
    fun objectMapper() = ObjectMapperProvider.objectMapper

    /**
     * Overskrever felles sin som bruker proxy, som ikke skal brukes på gcp
     */
    @Bean
    @Primary
    fun restTemplateBuilder(): RestTemplateBuilder {
        val jackson2HttpMessageConverter = MappingJackson2HttpMessageConverter(ObjectMapperProvider.objectMapper)
        return RestTemplateBuilder()
            .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(60, ChronoUnit.SECONDS))
            .messageConverters(listOf(jackson2HttpMessageConverter) + RestTemplate().messageConverters)
    }

    /**
     * Overskrever OAuth2HttpClient som settes opp i token-support som ikke kan få med objectMapper fra felles
     * pga .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
     * og [OAuth2AccessTokenResponse] som burde settes med setters, då feltnavn heter noe annet enn feltet i json
     */
    @Primary
    @Bean
    fun oAuth2HttpClient(): OAuth2HttpClient {
        val jackson2HttpMessageConverter = MappingJackson2HttpMessageConverter(ObjectMapperProvider.objectMapper)
        return RetryOAuth2HttpClient(
            RestClient.create(
                RestTemplateBuilder()
                    .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                    .readTimeout(Duration.of(4, ChronoUnit.SECONDS))
                    .messageConverters(listOf(jackson2HttpMessageConverter) + RestTemplate().messageConverters)
                    .build(),
            ),
        )
    }

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            try {
                SpringTokenValidationContextHolder()
                    .getTokenValidationContext()
                    .getClaims("azuread")
                    .getStringClaim("preferred_username")
            } catch (e: Exception) {
                throw e
            }

        override fun harTilgang(): Boolean {
            val grupper =
                Result
                    .runCatching { SpringTokenValidationContextHolder().getTokenValidationContext() }
                    .fold(
                        onSuccess = {
                            @Suppress("UNCHECKED_CAST")
                            val groups = it.getClaims("azuread").get("groups") as List<String>?
                            groups?.toSet() ?: emptySet()
                        },
                        onFailure = { emptySet() },
                    )

            return grupper.contains(prosesseringRolle)
        }
    }
}
