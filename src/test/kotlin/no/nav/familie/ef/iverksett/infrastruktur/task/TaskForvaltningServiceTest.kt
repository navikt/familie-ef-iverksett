package no.nav.familie.ef.iverksett.infrastruktur.task

import jakarta.annotation.PostConstruct
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.familie.prosessering.internal.TaskWorker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.UUID

class TaskForvaltningServiceTest : ServerTest() {
    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var taskForvaltningService: TaskForvaltningService

    @Autowired
    private lateinit var taskWorker: TaskWorker

    var taskId: Long = 0

    val payload = UUID.randomUUID().toString()

    @PostConstruct
    fun init() {
        val savedTask = taskService.save(lagTask())
        assertThat(savedTask.status).isEqualTo(Status.UBEHANDLET)
        taskId = savedTask.id
        kjørTaskSomFeilerTilStatusBlirMANUELL_OPPFØLGING(taskId)
    }

    @Test
    internal fun `Klon eksisterende task`() {
        val task = taskService.findById(taskId)
        assertThat(taskService.antallGangerPlukket(task.id)).isEqualTo(DistribuerVedtaksbrevTask.MAX_FORSØK)

        val kopi = taskForvaltningService.kopierTask(task)

        val orginalTaskFraDb = taskService.findById(taskId)

        println("orginalTaskFraDb ---------------------")
        println(orginalTaskFraDb)
        println("payload: ${orginalTaskFraDb.payload}")
        println(orginalTaskFraDb.metadata)
        println("slutt ---------------------")

        assertThat(LocalDateTime.parse(orginalTaskFraDb.payload)).isBeforeOrEqualTo(now())
        assertThat(taskService.antallGangerPlukket(kopi.id)).isEqualTo(0)
        assertThat(kopi.payload).isEqualTo(payload)
        assertThat(kopi.versjon).isEqualTo(1)

        assertThat(kopi.callId).isNotEqualTo(orginalTaskFraDb.callId)
    }

    @Test
    internal fun `Kast exception hvis ikke task er i manuell`() {
        val task = Task(type = JournalførVedtaksbrevTask.TYPE, payload = this.payload, status = Status.UBEHANDLET)
        taskService.save(task)

        assertThrows<IllegalStateException> { taskForvaltningService.kopierTask(task) }
    }

    private fun lagTask(): Task = Task(type = DistribuerVedtaksbrevTask.TYPE, payload = this.payload, status = Status.UBEHANDLET).medTriggerTid(now().minusDays(1))

    private fun kjørTaskSomFeilerTilStatusBlirMANUELL_OPPFØLGING(taskId: Long) {
        taskService.findById(taskId)
        val times = DistribuerVedtaksbrevTask.MAX_FORSØK + 1
        repeat(times) { doWork(taskId) }
    }

    fun doWork(taskId: Long) {
        try {
            taskWorker.markerPlukket(taskId)
            taskWorker.doActualWork(taskId)
        } catch (e: IllegalStateException) {
            // forventet å få feil her
            taskWorker.doFeilhåndtering(taskId, e)
        }
    }
}
