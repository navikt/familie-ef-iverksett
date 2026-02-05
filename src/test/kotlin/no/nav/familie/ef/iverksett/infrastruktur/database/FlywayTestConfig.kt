package no.nav.familie.ef.iverksett.infrastruktur.database

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Configuration
@Profile("local", "integrationtest")
class FlywayTestConfig {
    @Bean
    fun flyway(
        dataSource: DataSource,
    ): Flyway =
        Flyway
            .configure()
            .dataSource(dataSource)
            .placeholders(
                mapOf(
                    "ignoreIfLocal" to "-- ",
                ),
            ).load()
            .also { it.migrate() }
}
