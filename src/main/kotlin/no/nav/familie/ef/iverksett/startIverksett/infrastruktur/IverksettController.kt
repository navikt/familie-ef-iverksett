package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.startIverksett.tjeneste.IverksettService
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(
    consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    path = ["/api/iverksett"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class IverksettController(
    val vedtakstatistikkService: VedtakstatistikkService,
    val objectMapper: ObjectMapper,
    val iverksettService: IverksettService
) {

    @PostMapping("/start")
    fun iverksett(
        @RequestPart("data") iverksettJson: IverksettJson,
        @RequestPart("fil") fil: MultipartFile
    ) {
        iverksettService.startIverksetting(iverksettJson.toDomain(), opprettBrev(iverksettJson, fil))
    }

    @PostMapping("/vedtakstatistikk")
    fun sendStatistikk(@RequestPart("data") data: String) {
        vedtakstatistikkService.sendTilKafka(data)
    }

    private fun opprettBrev(iverksettJson: IverksettJson, fil: MultipartFile): Brev {
        return Brev(iverksettJson.behandlingId, fil.bytes)
    }


}