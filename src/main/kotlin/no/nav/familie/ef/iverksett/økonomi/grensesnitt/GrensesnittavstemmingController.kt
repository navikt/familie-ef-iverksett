package no.nav.familie.ef.iverksett.økonomi.grensesnitt

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerhetContext
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<Ressurs<String>> {
        SikkerhetContext.validerKallKommerFraEfSak()
        val stønadType = grensesnittavstemmingRequest.stønadType
        val eksisterendeGrensesnittAvstemmingTasker = taskService.finnTasksMedStatus(listOf(Status.UBEHANDLET, Status.KLAR_TIL_PLUKK), GrensesnittavstemmingTask.TYPE)
        eksisterendeGrensesnittAvstemmingTasker.forEach { task ->
            val payload = jsonMapper.readValue(task.payload, GrensesnittavstemmingPayload::class.java)
            if (payload.stønadstype == stønadType) {
                throw ApiFeil("Det finnes allerede en task for grensesnittavstemming for stønad=$stønadType", HttpStatus.BAD_REQUEST)
            }
        }
        val grensesnittavstemmingDto = GrensesnittavstemmingDto(grensesnittavstemmingRequest.stønadType, LocalDate.now(), LocalDateTime.now())
        taskService.save(grensesnittavstemmingDto.tilTask())
        return ResponseEntity.ok(Ressurs.success("OK"))
    }
}

data class GrensesnittavstemmingRequestDto(
    val stønadType: StønadType,
)
