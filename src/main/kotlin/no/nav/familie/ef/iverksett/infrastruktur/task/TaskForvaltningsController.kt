package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
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
    private val secureLogger = LoggerFactory.getLogger("secureLogger")


    @PostMapping("/restart/{taskId}")
    @Transactional
    fun hentStatus(
        @PathVariable taskId: Long,
    ): ResponseEntity<String> {

        logger.info("Starter kloning av task id ${taskId}.")

        val task = taskService.findById(taskId)

        check(task.status==Status.MANUELL_OPPFØLGING) { "Task må ha status MANUELL_OPPFØLGING" }
        logger.info("Fant task: ${task.id} med type ${task.type}.")

        val annotation = task::class.findAnnotation<TaskStepBeskrivelse>()
        val maxAntallFeil = annotation?.maxAntallFeil ?: 3
        val antallGangerPlukket = taskService.antallGangerPlukket(taskId)

        logger.info("Tasken har blitt plukket $antallGangerPlukket ganger. Maks antall feil tillatt: $maxAntallFeil.")

        check(antallGangerPlukket > maxAntallFeil){"Tasken kan ikke plukkes på nytt, da den ikke er plukket for mange ganger: $antallGangerPlukket"}

        val nyTask = task.copy(id = 0L, triggerTid = LocalDateTime.now().plusMinutes(15))


        secureLogger.info(nyTask.toString())

        taskService.save(task.copy(payload = task.payload+"-konet"))
        val lagretTask = taskService.save(nyTask) // For å restarte tasken, må vi sette id til 0, slik at den blir opprettet på nytt

        logger.info("Kloner task med id ${task.id}. Opprettet ny task: ${lagretTask.id}")
        return ResponseEntity("OK - opprettet ${lagretTask.id}", HttpStatus.OK)
    }
}
