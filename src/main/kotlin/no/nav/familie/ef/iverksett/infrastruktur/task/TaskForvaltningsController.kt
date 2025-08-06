package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.reflect.full.findAnnotation

@RestController
@RequestMapping(
    path = ["/api/forvaltning/task"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class TaskForvaltningsController(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)


    @PostMapping("/restart/{taskId}")
    fun hentStatus(
        @PathVariable taskId: Long,
    ): ResponseEntity<String> {
        val task = taskService.findById(taskId)

        val annotation = task::class.findAnnotation<TaskStepBeskrivelse>()
        val maxAntallFeil = annotation?.maxAntallFeil ?: 3
        val antallGangerPlukket = taskService.antallGangerPlukket(taskId)

        check(antallGangerPlukket > maxAntallFeil){"Tasken kan ikke plukkes på nytt, da den ikke er plukket for mange ganger: $antallGangerPlukket"}

        val nyTask = task.copy(id = 0L, triggerTid = LocalDateTime.now().plusMinutes(15))
        val lagretTask = taskService.save(nyTask) // For å restarte tasken, må vi sette id til 0, slik at den blir opprettet på nytt

        logger.info("Kloner task med id ${task.id}. Opprettet ny task: ${lagretTask.id}")
        return ResponseEntity("OK - opprettet ${lagretTask.id}", HttpStatus.OK)
    }
}
