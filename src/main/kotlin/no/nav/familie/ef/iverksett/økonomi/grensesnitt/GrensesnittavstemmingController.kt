package no.nav.familie.ef.iverksett.økonomi.grensesnitt

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerthetContext
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/grensesnittavstemming")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class GrensesnittavstemmingController(
    private val taskService: TaskService,
) {
    @PostMapping
    fun startGrensesnittavstemmingForStønad(
        @RequestBody grensesnittavstemmingRequest: GrensesnittavstemmingRequestDto,
    ) {
        if (!SikkerthetContext.kallKommerFraEfSak()) {
            throw ApiFeil("Kall kommer ikke fra ef-sak", HttpStatus.FORBIDDEN)
        }
        val stønadType = grensesnittavstemmingRequest.stønadType
        val eksisterendeGrensesnittAvstemmingTasker = taskService.finnTasksMedStatus(listOf(Status.UBEHANDLET, Status.KLAR_TIL_PLUKK), GrensesnittavstemmingTask.TYPE)
        eksisterendeGrensesnittAvstemmingTasker.forEach { task ->
            val payload = objectMapper.readValue<GrensesnittavstemmingPayload>(task.payload)
            if (payload.stønadstype == stønadType) {
                throw ApiFeil("Det finnes allerede en task for grensesnittavstemming for stønad=$stønadType", HttpStatus.BAD_REQUEST)
            }
        }
        val grensesnittavstemmingDto = GrensesnittavstemmingDto(grensesnittavstemmingRequest.stønadType, LocalDate.now(), LocalDateTime.now())
        taskService.save(grensesnittavstemmingDto.tilTask())
    }
}

data class GrensesnittavstemmingRequestDto(
    val stønadType: StønadType,
)
