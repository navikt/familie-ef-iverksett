package no.nav.familie.ef.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfiguration {

    @Bean
    open fun objectMapperBuilder(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

}