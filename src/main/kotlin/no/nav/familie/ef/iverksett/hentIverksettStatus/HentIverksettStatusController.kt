package no.nav.familie.ef.iverksett.hentIverksettStatus

import no.nav.familie.ef.iverksett.domene.IverksettStatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        path = ["/api/iverksett"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class HentIverksettStatusController(
        val hentIverksettStatusService: HentIverksettStatusService
) {

    @GetMapping("/status/{behandlingId}")
    fun hentStatus(@PathVariable behandlingId: UUID): ResponseEntity<IverksettStatus> {
        val status = hentIverksettStatusService.utledStatus(behandlingId)
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }
}
