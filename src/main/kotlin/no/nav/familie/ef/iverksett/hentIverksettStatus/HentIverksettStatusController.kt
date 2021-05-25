package no.nav.familie.ef.iverksett.hentIverksettStatus

import no.nav.familie.ef.iverksett.domene.IverksettStatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

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

    @GetMapping("/hentstatus/{behandlingId}")
    fun hentStatus(@PathVariable behandlingId: UUID): IverksettStatus {
        return hentIverksettStatusService.utledStatus(behandlingId)
    }
}
