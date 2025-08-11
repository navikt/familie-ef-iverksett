package no.nav.familie.ef.iverksett

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.familie.ef.iverksett.infrastruktur.configuration.ApplicationConfig
import no.nav.familie.ef.iverksett.infrastruktur.configuration.RolleConfig
import no.nav.familie.ef.iverksett.infrastruktur.database.DbContainerInitializer
import no.nav.familie.ef.iverksett.util.TokenUtil
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@SpringBootTest(classes = [ApplicationLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("servertest", "mock-oppdrag", "mock-kafkatemplate", "mock-tilbakekreving", "mock-oauth", "mock-integrasjoner")
@EnableMockOAuth2Server
abstract class ServerTest {
    protected val restTemplate = TestRestTemplate(ApplicationConfig().restTemplateBuilder())
    protected val headers = HttpHeaders()

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @LocalServerPort
    private var port: Int? = 0

    @Autowired
    private lateinit var rolleConfig: RolleConfig

    @AfterEach
    fun reset() {
        headers.clear()
        resetDatabase()
        resetWiremockServers()
    }

    private fun resetWiremockServers() {
        applicationContext.getBeansOfType(WireMockServer::class.java).values.forEach(WireMockServer::resetRequests)
    }

    private fun resetDatabase() {
        namedParameterJdbcTemplate.update(
            "TRUNCATE TABLE brev, iverksett, iverksett_resultat CASCADE",
            MapSqlParameterSource(),
        )
        namedParameterJdbcTemplate.update("TRUNCATE TABLE task, task_logg CASCADE", MapSqlParameterSource())
        namedParameterJdbcTemplate.update("TRUNCATE TABLE frittstaende_brev", MapSqlParameterSource())
        namedParameterJdbcTemplate.update("TRUNCATE TABLE aktivitetsplikt_brev", MapSqlParameterSource())
    }

    protected fun getPort(): String = port.toString()

    protected fun localhostUrl(uri: String): String = "http://localhost:" + getPort() + uri

    protected fun onBehalfOfToken(
        roles: List<String>,
        saksbehandler: String = "julenissen",
    ): String = TokenUtil.onBehalfOfToken(mockOAuth2Server, roles, saksbehandler)

    protected fun oboBearerToken(): String = onBehalfOfToken(roles = listOf(rolleConfig.forvalter, rolleConfig.prosessering))
}
