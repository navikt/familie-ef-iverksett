package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerthetContext
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk/vedtakstatistikk"])
@ProtectedWithClaims(issuer = "azuread")
@Profile("dev", "local")
class VedtakstatistikkTestController(
    private val vedtakstatistikkService: VedtakstatistikkService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(
        @RequestBody data: IverksettDto,
    ) {
        if (!SikkerthetContext.kallKommerFraEfSak()) {
            logger.error("Kall kommer ikke fra ef-sak")
            throw ApiFeil("Kall kommer ikke fra ef-sak", HttpStatus.FORBIDDEN)
        }

        vedtakstatistikkService.sendTilKafka(data.toDomain(), null)
    }
}
