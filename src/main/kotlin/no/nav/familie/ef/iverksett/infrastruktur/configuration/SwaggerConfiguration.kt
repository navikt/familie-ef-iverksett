package no.nav.familie.ef.iverksett.infrastruktur.configuration

import com.google.common.base.Predicate
import com.google.common.base.Predicates.or
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfiguration {

    @Bean
    fun postsApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2).groupName("teamfamilie").apiInfo(apiInfo()).select().paths(postPaths()).build()
    }

    private fun postPaths(): Predicate<String> {
        return or(regex("/api.*"))
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder().title("Iverksett API").version("0.1")
                .description("")
                .build()
    }

}