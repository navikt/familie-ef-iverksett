package no.nav.familie.ef.iverksett.økonomi.konsistens

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/konsistensavstemming")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class KonsistensavstemmingController(
    private val konsistensavstemmingService: KonsistensavstemmingService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val teamLogsMarker = MarkerFactory.getMarker("TEAM_LOGS")

    // TODO må verifisere at man ikke lagrer ned kildeBehandlingId i iverksett
    // Og må verifisere at man ikke bruker kildeBehandlingId på andelsnivå fra KonsistensavstemmingDto
    @PostMapping
    fun startKonsistensavstemming(
        @RequestBody konsistensavstemmingDto: KonsistensavstemmingDto,
        @RequestParam(name = "sendStartmelding") sendStartmelding: Boolean = true,
        @RequestParam(name = "sendAvsluttmelding") sendAvsluttmelding: Boolean = true,
        @RequestParam(name = "transaksjonId") transaksjonId: UUID? = null,
    ) {
        if (!SikkerhetContext.kallKommerFraEfSak()) {
            logger.error(teamLogsMarker, "Kall kommer ikke fra ef-sak")
            throw ApiFeil("Kall kommer ikke fra ef-sak", HttpStatus.FORBIDDEN)
        }

        konsistensavstemmingService.sendKonsistensavstemming(
            konsistensavstemmingDto,
            sendStartmelding,
            sendAvsluttmelding,
            transaksjonId,
        )
    }

    @GetMapping("timeout-test", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun timeoutTest(
        @RequestParam(name = "sekunder") sekunder: Long,
    ): String {
        if (!SikkerhetContext.kallKommerFraEfSak() && !SikkerhetContext.kallKommerFraFraProsessering()) {
            logger.error(teamLogsMarker, "Kall kommer ikke fra ef-sak eller familie-prosessering")
            throw ApiFeil("Kall kommer ikke fra ef-sak eller familie-prosessering", HttpStatus.FORBIDDEN)
        }

        return konsistensavstemmingService.testTimeout(sekunder)
    }
}
