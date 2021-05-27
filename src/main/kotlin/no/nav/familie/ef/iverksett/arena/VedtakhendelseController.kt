package no.nav.familie.ef.iverksett.arena


import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/iverksett"])
@ProtectedWithClaims(issuer = "azuread")
class VedtakhendelseController(
    val vedtakhendelseProducer: VedtakhendelseProducer,
) {

    @PostMapping("/vedtakhendelse", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(@RequestBody data: String) {
        vedtakhendelseProducer.produce("testmelding $data" )
    }

}