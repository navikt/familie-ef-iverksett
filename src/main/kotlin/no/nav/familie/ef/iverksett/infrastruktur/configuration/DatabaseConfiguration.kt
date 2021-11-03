package no.nav.familie.ef.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@EnableJdbcRepositories("no.nav.familie")
class DatabaseConfiguration : AbstractJdbcConfiguration() {

    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(listOf(PropertiesWrapperTilStringConverter(),
                                            StringTilPropertiesWrapperConverter(),
                                            BehandlingDVHTilStringConverter(),
                                            StringTilBehandlingDVHConverter()))
    }

    @WritingConverter
    class BehandlingDVHTilStringConverter : Converter<BehandlingDVH, PGobject> {

        override fun convert(utbetalingsoppdrag: BehandlingDVH): PGobject =
                PGobject().apply {
                    type = "json"
                    value = objectMapper.writeValueAsString(utbetalingsoppdrag)
                }

    }

    @ReadingConverter
    class StringTilBehandlingDVHConverter : Converter<PGobject, BehandlingDVH> {

        override fun convert(pgObject: PGobject): BehandlingDVH {
            return objectMapper.readValue(pgObject.value!!)
        }
    }
}
