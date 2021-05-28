package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/iverksett/vedtakstatistikk"])
@ProtectedWithClaims(issuer = "azuread")
class VedtakstatistikkController(
        val vedtakstatistikkService: VedtakstatistikkService,
) {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(@RequestBody data: IverksettDto) {
        vedtakstatistikkService.sendTilKafka(data.toDomain())
    }

}