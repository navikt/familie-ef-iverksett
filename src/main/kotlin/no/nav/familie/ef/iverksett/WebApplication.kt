package no.nav.familie.ef.iverksett

import no.nav.familie.ef.iverksett.infrastruktur.configuration.MiljoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class WebApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(WebApplication::class.java)
                    .profiles(*MiljoConfiguration.profiles())
                    .run(*args)
        }
    }
}


