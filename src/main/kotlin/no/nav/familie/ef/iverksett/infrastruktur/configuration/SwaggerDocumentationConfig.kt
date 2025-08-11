package no.nav.familie.ef.iverksett.infrastruktur.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerDocumentationConfig(
    @Value("\${AUTHORIZATION_URL}")
    val authorizationUrl: String,
    @Value("\${AZUREAD_TOKEN_ENDPOINT_URL}")
    val tokenUrl: String,
    @Value("\${API_SCOPE}")
    val apiScope: String,
) {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .components(Components().addSecuritySchemes("oauth2", securitySchemes()))
            .addSecurityItem(SecurityRequirement().addList("oauth2", listOf("read", "write")))

    @Bean
    fun eksternOpenApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("Forvalter - task")
            .packagesToScan("no.nav.familie.ef.iverksett.infrastruktur.task")
            .build()

    @Bean
    fun internOpenApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("Alle")
            .packagesToScan("no.nav.familie.ef.iverksett")
            .build()

    private fun securitySchemes(): SecurityScheme =
        SecurityScheme()
            .name("oauth2")
            .type(SecurityScheme.Type.OAUTH2)
            .scheme("oauth2")
            .`in`(SecurityScheme.In.HEADER)
            .flows(
                OAuthFlows()
                    .authorizationCode(
                        OAuthFlow()
                            .authorizationUrl(authorizationUrl)
                            .tokenUrl(tokenUrl)
                            .scopes(Scopes().addString(apiScope, "read,write")),
                    ),
            )
}

// @Configuration
// class SwaggerConfiguration {
//    private val bearer = "Bearer"
//
//    @Bean
//    fun openApi(): OpenAPI =
//        OpenAPI()
//            .info(Info().title("Familie ef sak api"))
//            .components(Components().addSecuritySchemes(bearer, bearerTokenSecurityScheme()))
//            .addSecurityItem(SecurityRequirement().addList(bearer, listOf("read", "write")))
//
//    private fun bearerTokenSecurityScheme(): SecurityScheme =
//        SecurityScheme()
//            .type(SecurityScheme.Type.APIKEY)
//            .scheme(bearer)
//            .bearerFormat("JWT")
//            .`in`(SecurityScheme.In.HEADER)
//            .name("Authorization")
// }
