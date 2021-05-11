package no.nav.familie.ef.iverksett.hentIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.configuration.ObjectMapperConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
@Import(ObjectMapperConfiguration::class)
class HentIverksettConfiguration {

    @Bean
    fun hentIverksettJdbc(namedParameterJdbcTemplate: NamedParameterJdbcTemplate, objectMapper: ObjectMapper): HentIverksettJdbc {
        return HentIverksettJdbc(namedParameterJdbcTemplate, objectMapper)
    }

    @Bean
    fun hentIverksettService(hentIverksettJdbc: HentIverksettJdbc): HentIverksettService {
        return HentIverksettService(hentIverksettJdbc)
    }
}