package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
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
        val iverksettService: IverksettService
) {

    @PostMapping
    fun iverksett(
            @RequestPart("data") iverksettDto: IverksettDto,
            @RequestPart("fil") fil: MultipartFile
    ) {
        iverksettService.startIverksetting(iverksettDto.toDomain(), opprettBrev(iverksettDto, fil))
    }

    private fun opprettBrev(iverksettDto: IverksettDto, fil: MultipartFile): Brev {
        return Brev(iverksettDto.behandling.behandlingId, fil.bytes)
    }


}