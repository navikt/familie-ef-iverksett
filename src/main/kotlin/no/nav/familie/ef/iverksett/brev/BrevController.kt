package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevService
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.ef.felles.PeriodiskAktivitetspliktBrevDto
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/brev"])
@PreAuthorize("hasRole('APPLICATION') or hasRole('SAKSBEHANDLER')")
class BrevController(
    private val frittståendeBrevService: FrittståendeBrevService,
) {
    @PostMapping("/frittstaende")
    fun distribuerFrittståendeBrev(
        @RequestBody data: FrittståendeBrevDto,
    ): ResponseEntity<Ressurs<String>> {
        SikkerhetContext.validerKallKommerFraEfSak()
        frittståendeBrevService.opprettTask(data)
        return ResponseEntity.ok(Ressurs.success("OK"))
    }

    @PostMapping("/frittstaende/innhenting-aktivitetsplikt")
    fun journalførBrevForInnhentingAvAktivitetsplikt(
        @RequestBody data: PeriodiskAktivitetspliktBrevDto,
    ): ResponseEntity<Ressurs<String>> {
        SikkerhetContext.validerKallKommerFraEfSak()
        frittståendeBrevService.opprettTaskForInnhentingAvAktivitetsplikt(data)
        return ResponseEntity.ok(Ressurs.success("OK"))
    }
}
