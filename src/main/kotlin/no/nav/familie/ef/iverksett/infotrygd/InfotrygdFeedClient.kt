package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.ef.iverksett.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.ef.infotrygd.OpprettPeriodeHendelseDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class InfotrygdFeedClient(
    @Value("\${INFOTRYGD_FEED_API_URL}")
    private val infotrygdFeedUri: URI,
    @Qualifier("azure")
    restOperations: RestOperations,
) : AbstractPingableRestClient(restOperations, "infotrygd.feed") {
    val opprettPeriodeUri: URI =
        UriComponentsBuilder
            .fromUri(infotrygdFeedUri)
            .pathSegment("api/entry/periode")
            .build()
            .toUri()

    fun opprettPeriodeHendelse(hendelseDto: OpprettPeriodeHendelseDto) {
        val headers =
            HttpHeaders().apply {
                accept = listOf(MediaType.TEXT_PLAIN)
            }
        postForEntity<Any>(opprettPeriodeUri, hendelseDto, headers)
    }

    override val pingUri: URI
        get() =
            UriComponentsBuilder
                .fromUri(infotrygdFeedUri)
                .pathSegment("api/ping")
                .build()
                .toUri()
}
