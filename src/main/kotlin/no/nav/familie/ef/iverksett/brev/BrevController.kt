package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevService
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.ef.felles.PeriodiskAktivitetspliktBrevDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
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
    ) {
        if (!SikkerhetContext.kallKommerFraEfSak()) {
            throw ApiFeil("Kall kommer ikke fra ef-sak", HttpStatus.FORBIDDEN)
        }

        frittståendeBrevService.opprettTask(data)
    }

    @PostMapping("/frittstaende/innhenting-aktivitetsplikt")
    fun journalførBrevForInnhentingAvAktivitetsplikt(
        @RequestBody data: PeriodiskAktivitetspliktBrevDto,
    ) {
        if (!SikkerhetContext.kallKommerFraEfSak()) {
            throw ApiFeil("Kall kommer ikke fra ef-sak", HttpStatus.FORBIDDEN)
        }

        frittståendeBrevService.opprettTaskForInnhentingAvAktivitetsplikt(data)
    }
}
