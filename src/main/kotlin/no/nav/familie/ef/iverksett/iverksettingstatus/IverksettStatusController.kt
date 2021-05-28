package no.nav.familie.ef.iverksett.iverksettingstatus

import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/iverksett/status"])
@ProtectedWithClaims(issuer = "azuread")
class IverksettStatusController(
        val iverksettStatusService: IverksettStatusService
) {

    @GetMapping("/{behandlingId}")
    fun hentStatus(@PathVariable behandlingId: UUID): ResponseEntity<IverksettStatus> {
        val status = iverksettStatusService.utledStatus(behandlingId)
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }
}
