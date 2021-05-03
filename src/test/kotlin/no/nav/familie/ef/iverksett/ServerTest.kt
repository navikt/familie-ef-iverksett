package no.nav.familie.ef.iverksett

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.familie.ef.iverksett.infrastruktur.database.DbContainerInitializer
import no.nav.familie.ef.iverksett.util.onBehalfOfToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@SpringBootTest(classes = [ApplicationLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("servertest", "mock-oppdrag")
abstract class ServerTest {

    protected val restTemplate = TestRestTemplate()
    protected val headers = HttpHeaders()

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @LocalServerPort
    private var port: Int? = 0

    @AfterEach
    fun reset() {
        resetWiremockServers()
    }

    private fun resetWiremockServers() {
        applicationContext.getBeansOfType(WireMockServer::class.java).values.forEach(WireMockServer::resetRequests)
    }

    protected fun getPort(): String {
        return port.toString()
    }

    protected fun localhostUrl(uri: String): String {
        return "http://localhost:" + getPort() + uri
    }

    protected val lokalTestToken: String
        get() {
            return onBehalfOfToken()
        }

}