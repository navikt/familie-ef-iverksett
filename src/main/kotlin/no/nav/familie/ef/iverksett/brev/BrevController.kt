package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevService
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.ef.felles.PeriodiskAktivitetspliktBrevDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/brev"])
@ProtectedWithClaims(issuer = "azuread")
class BrevController(
    private val frittståendeBrevService: FrittståendeBrevService,
) {
    @PostMapping("/frittstaende")
    fun distribuerFrittståendeBrev(
        @RequestBody data: FrittståendeBrevDto,
    ): ResponseEntity<Any> {
        frittståendeBrevService.opprettTask(data)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/frittstaende/innhenting-aktivitetsplikt")
    fun journalførBrevForInnhentingAvAktivitetsplikt(
        @RequestBody data: PeriodiskAktivitetspliktBrevDto,
    ): Ressurs<Unit> {
        frittståendeBrevService.opprettTaskForInnhentingAvAktivitetsplikt(data)
        return Ressurs.success(Unit)
    }
}
