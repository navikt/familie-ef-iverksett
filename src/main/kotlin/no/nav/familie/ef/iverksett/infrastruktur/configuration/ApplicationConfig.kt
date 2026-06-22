package no.nav.familie.ef.iverksett.infrastruktur.configuration

import no.nav.familie.ef.iverksett.util.JsonMapperProvider
import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.familie.sikkerhet.context.FamilieFellesSpringSecurityKonfigurasjon
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@SpringBootConfiguration
@ConfigurationPropertiesScan("no.nav.familie.ef.iverksett")
@ComponentScan(
    "no.nav.familie.ef.iverksett",
    "no.nav.familie.prosessering",
    "no.nav.familie.sikkerhet",
    "no.nav.familie.unleash",
    "no.nav.familie.felles.tokenklient",
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [MappingJackson2XmlHttpMessageConverter::class]),
    ],
)
@Import(
    KafkaErrorHandler::class,
    FamilieFellesSpringSecurityKonfigurasjon::class,
)
@EnableScheduling
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

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
    fun jsonMapper() = JsonMapperProvider.jsonMapper

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication is JwtAuthenticationToken) {
                return authentication.token.getClaimAsString("preferred_username")
                    ?: error("Finner ikke preferred_username i token")
            }

            error("Finner ikke brukernavn i security context")
        }

        override fun harTilgang(): Boolean {
            val authentication = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken
            val grupper = authentication?.token?.getClaimAsStringList("groups")?.toSet() ?: emptySet()

            return grupper.contains(prosesseringRolle)
        }
    }
}
