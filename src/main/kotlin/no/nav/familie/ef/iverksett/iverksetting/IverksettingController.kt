package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping(
        path = ["/api/iverksett"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class IverksettingController(
        val iverksettingService: IverksettingService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun iverksett(
            @RequestPart("data") iverksettDto: IverksettDto,
            @RequestPart("fil") fil: MultipartFile
    ) {
        iverksettingService.startIverksetting(iverksettDto.toDomain(), opprettBrev(iverksettDto, fil))
    }

    @GetMapping("/status/{behandlingId}")
    fun hentStatus(@PathVariable behandlingId: UUID): ResponseEntity<IverksettStatus> {
        val status = iverksettingService.utledStatus(behandlingId)
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @PostMapping("/hdsjhkdsa/{behandlingId}")
    fun removeMe(@RequestBody json : IverksettDto,@PathVariable behandlingId: UUID): ResponseEntity<IverksettStatus> {
            val status = iverksettingService.utledStatus(behandlingId)
            return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }
    private fun opprettBrev(iverksettDto: IverksettDto, fil: MultipartFile): Brev {
        return Brev(iverksettDto.behandling.behandlingId, fil.bytes)
    }


}