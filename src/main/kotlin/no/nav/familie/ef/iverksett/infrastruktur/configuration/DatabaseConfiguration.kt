package no.nav.familie.ef.iverksett.infrastruktur.configuration

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration {
    private val logger = LoggerFactory.getLogger(this::class.java)
    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        logger.info("Datasource : ${dataSource.toString()}")
        return DataSourceTransactionManager(dataSource)
    }
}