package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk/vedtakstatistikk"])
@PreAuthorize("hasRole('BESLUTTER') or hasRole('SAKSBEHANDLER') or hasRole('APPLICATION')")
@Profile("dev", "local")
class VedtakstatistikkTestController(
    private val vedtakstatistikkService: VedtakstatistikkService,
) {
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(
        @RequestBody data: IverksettDto,
    ): ResponseEntity<Ressurs<String>> {
        SikkerhetContext.validerKallKommerFraEfSak()
        vedtakstatistikkService.sendTilKafka(data.toDomain(), null)
        return ResponseEntity.ok(Ressurs.success("OK"))
    }
}
