package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.infotrygd.SendFattetVedtakTilInfotrygdTask
import no.nav.familie.ef.iverksett.infotrygd.SendPerioderTilInfotrygdTask
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.ef.iverksett.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import java.time.LocalDateTime

class TaskType(
        val type: String,
        val triggerTidAntallMinutterFrem: Long? = null
)

fun hovedflyt() = listOf(
        TaskType(IverksettMotOppdragTask.TYPE),
        TaskType(VentePåStatusFraØkonomiTask.TYPE, 15),
        TaskType(JournalførVedtaksbrevTask.TYPE),
        TaskType(DistribuerVedtaksbrevTask.TYPE),
        TaskType(SendFattetVedtakTilInfotrygdTask.TYPE)
)

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this.type == it.first.type }.second


fun Task.opprettNesteTask(): Task {
    val nesteTask = TaskType(this.type).nesteHovedflytTask()

    return if (nesteTask.triggerTidAntallMinutterFrem != null) {
        Task(type = nesteTask.type,
             payload = this.payload,
             triggerTid = LocalDateTime.now().plusMinutes(nesteTask.triggerTidAntallMinutterFrem))
    } else {
        Task(type = nesteTask.type,
             payload = this.payload,
             properties = this.metadata)
    }

}
