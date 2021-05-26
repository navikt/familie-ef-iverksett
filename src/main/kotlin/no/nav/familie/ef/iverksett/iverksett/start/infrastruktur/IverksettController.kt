package no.nav.familie.ef.iverksett.iverksett.start.infrastruktur

import no.nav.familie.ef.iverksett.iverksett.Brev
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettDto
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.iverksett.start.tjeneste.IverksettService
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
    val iverksettService: IverksettService
) {

    @PostMapping("/start")
    fun iverksett(
            @RequestPart("data") iverksettDto: IverksettDto,
            @RequestPart("fil") fil: MultipartFile
    ) {
        iverksettService.startIverksetting(iverksettDto.toDomain(), opprettBrev(iverksettDto, fil))
    }

    @PostMapping("/vedtakstatistikk", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(@RequestBody data: IverksettDto) {
        vedtakstatistikkService.sendTilKafka(data.toDomain())
    }

    private fun opprettBrev(iverksettDto: IverksettDto, fil: MultipartFile): Brev {
        return Brev(iverksettDto.behandling.behandlingId, fil.bytes)
    }


}