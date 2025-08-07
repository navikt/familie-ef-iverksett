package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@Service
class TaskForvaltningService(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    fun kopierTask(task: Task): Task {
        check(task.status == Status.MANUELL_OPPFØLGING) { "Task som skal kopieres må ha status MANUELL_OPPFØLGING" }
        val nyTask = task.copy(id = 0L, versjon = 0L, triggerTid = LocalDateTime.now().plusMinutes(15))

        taskService.save(task.copy(payload = "${task.payload}-klonetTilNy"))
        secureLogger.info(nyTask.toString())
        val lagretTask = taskService.save(nyTask)

        logger.info("Kloner task med id ${task.id}. Opprettet ny task: ${lagretTask.id}")
        return lagretTask
    }
}

@RestController
@RequestMapping(
    path = ["/api/forvaltning/task"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class TaskForvaltningsController(
    private val taskService: TaskService,
    private val taskForvaltningService: TaskForvaltningService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/restart/{taskId}")
    fun hentStatus(
        @PathVariable taskId: Long,
    ): ResponseEntity<String> {
        logger.info("Starter kloning av task id $taskId.")
        val task = taskService.findById(taskId)
        val lagretTask = taskForvaltningService.kopierTask(task)
        return ResponseEntity("OK - opprettet ${lagretTask.id}, fra ${task.id}", HttpStatus.OK)
    }
}
