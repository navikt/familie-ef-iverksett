package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettDto
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/iverksett"])
@ProtectedWithClaims(issuer = "azuread")
class VedtakstatistikkController(
        val vedtakstatistikkService: VedtakstatistikkService,
) {

    @PostMapping("/vedtakstatistikk", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(@RequestBody data: IverksettDto) {
        vedtakstatistikkService.sendTilKafka(data.toDomain())
    }

}