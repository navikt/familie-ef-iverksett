package no.nav.familie.ef.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean

@SpringBootConfiguration
@ConfigurationPropertiesScan
class ApplicationConfig {

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule()
}