package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(
    path = ["/api/arbeidsoppfolging"],
)
@Unprotected
class PatchArbeidsoppfølgingController(
    val iverksettingRepository: IverksettingRepository,
    val arbeidsoppfølgingService: ArbeidsoppfølgingService,
) {

    @PostMapping("/patch/{behandlingId}")
    fun publiserVedtakshendelser(@PathVariable behandlingId: UUID): ResponseEntity<Any> {
        val iverksettData = iverksettingRepository.findByIdOrThrow(behandlingId).data //Kall fra ef-sak kommer fra task
        arbeidsoppfølgingService.sendTilKafka(iverksettData)
        return ResponseEntity.ok().build()
    }
}
