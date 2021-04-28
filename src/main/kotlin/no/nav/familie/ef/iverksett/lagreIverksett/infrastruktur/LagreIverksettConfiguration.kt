package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
class LagreIverksettConfiguration {

    @Bean
    fun lagreIverksettJdbc(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreIverksettJdbc {
        return LagreIverksettJdbc(namedParameterJdbcTemplate)
    }
    @Bean
    fun lagreIverksettService(lagreIverksettJdbc: LagreIverksettJdbc) : LagreIverksettService {
        return LagreIverksettService(lagreIverksettJdbc)
    }
}