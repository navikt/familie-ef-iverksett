package no.nav.familie.ef.iverksett.infrastruktur.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration {

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource) : NamedParameterJdbcTemplate {
        return namedParameterJdbcTemplate(dataSource)
    }
}