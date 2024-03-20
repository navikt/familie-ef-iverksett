package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
    @GetMapping("/patch/")
    fun publiserVedtakshendelser(): ResponseEntity<Any> {
        val behandlingId = UUID.fromString("202cbaed-d92f-406f-b208-7a84d014df65")
        val iverksettData = iverksettingRepository.findByIdOrThrow(behandlingId).data
        arbeidsoppfølgingService.sendTilKafka(iverksettData)
        return ResponseEntity.ok().build()
    }
}
