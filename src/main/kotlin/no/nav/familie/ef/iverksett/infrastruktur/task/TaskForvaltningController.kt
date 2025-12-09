package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.sikkerhet.SikkerthetContext
import no.nav.familie.prosessering.domene.PropertiesWrapper
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime.now
import java.util.Properties

@RestController
@RequestMapping(
    path = ["/api/forvaltning/task"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class TaskForvaltningController(
    private val taskService: TaskService,
    private val taskForvaltningService: TaskForvaltningService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/restart/{taskId}")
    fun kopierTaskStartPåNytt(
        @PathVariable taskId: Long,
    ): KopiertTaskResponse {
        if (!SikkerthetContext.kallKommerFraFraProsessering()) {
            throw ApiFeil("Kall kommer ikke fra familie-prosessering", HttpStatus.FORBIDDEN)
        }
        logger.info("Starter kloning av task id $taskId.")
        val task = taskService.findById(taskId)
        val kopiertTilTask = taskForvaltningService.kopierTask(task)
        logger.info("Kopiert til task id: ${kopiertTilTask.id}.")
        return KopiertTaskResponse(task.id, kopiertTilTask.id)
    }
}

data class KopiertTaskResponse(
    val fraTaskId: Long,
    val tilNyTaskId: Long,
)

@Service
class TaskForvaltningService(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun kopierTask(task: Task): Task {
        check(task.status == Status.MANUELL_OPPFØLGING) { "Task som skal kopieres må ha status MANUELL_OPPFØLGING" }

        val kopierProperties = kopierProperties(task)
        val kopiertTask = task.copy(id = 0L, versjon = 0L, triggerTid = now().plusMinutes(15), metadataWrapper = PropertiesWrapper(kopierProperties))

        val gamleProperties = oppdaterGamleProperties(task)
        taskService.save(task.copy(payload = "${now()}", metadataWrapper = PropertiesWrapper(gamleProperties)))

        val lagretTask = taskService.save(kopiertTask)
        logger.info("Klonet task med id ${task.id}. Opprettet ny task: ${lagretTask.id}")
        return lagretTask
    }

    private fun oppdaterGamleProperties(task: Task): Properties {
        val oldProps = Properties()
        oldProps.putAll(task.metadata)
        oldProps.setProperty("Info", "Kopiert - kan avvikshåndteres")
        oldProps.setProperty("GammelPayload", task.payload)
        return oldProps
    }

    private fun kopierProperties(task: Task): Properties {
        val newProps = Properties()
        newProps.putAll(task.metadata)
        newProps.setProperty("Info", "Kopiert fra task med id ${task.id}")
        return newProps
    }
}
