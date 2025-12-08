package no.nav.familie.ef.iverksett.infrastruktur.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SwaggerConfiguration(
    @Value("\${AUTHORIZATION_URL}")
    val authorizationUrl: String,
    @Value("\${AZUREAD_TOKEN_ENDPOINT_URL}")
    val tokenUrl: String,
    @Value("\${API_SCOPE}")
    val apiScope: String,
) {
    private val preprodServer: Server = Server().description("Pre-prod")

    @Bean
    open fun swaggerApiConfig(): OpenAPI =
        OpenAPI()
            .components(Components().addSecuritySchemes("oauth2", securitySchemes()))
            .addSecurityItem(SecurityRequirement().addList("oauth2", listOf("read", "write")))
            .info(
                Info()
                    .title("Familie-EF-Iverksett")
                    .description("Swagger for Familie-EF-Iverksett")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Team Etterlatte")
                            .url("https://github.com/navikt/gjenlevende-bs-sak"),
                    ),
            ).servers(listOf(preprodServer))

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
