package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.ef.iverksett.arena.SendFattetVedtakTilArenaTask
import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.infotrygd.SendFattetVedtakTilInfotrygdTask
import no.nav.familie.ef.iverksett.infotrygd.SendPerioderTilInfotrygdTask
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad
import no.nav.familie.ef.iverksett.vedtak.PubliserVedtakTilKafkaTask
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkTask
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.ef.iverksett.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import java.time.LocalDateTime

class TaskType(
        val type: String,
        val triggerTidAntallSekunderFrem: Long? = null
)

fun hovedflyt() = listOf(
        TaskType(IverksettMotOppdragTask.TYPE),
        TaskType(VentePåStatusFraØkonomiTask.TYPE, 20),
        TaskType(JournalførVedtaksbrevTask.TYPE),
        TaskType(DistribuerVedtaksbrevTask.TYPE),
)

fun publiseringsflyt() = listOf(
        TaskType(SendFattetVedtakTilInfotrygdTask.TYPE),
        TaskType(SendPerioderTilInfotrygdTask.TYPE),
        TaskType(SendFattetVedtakTilArenaTask.TYPE),
        TaskType(PubliserVedtakTilKafkaTask.TYPE),
        TaskType(OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad.TYPE),
        TaskType(VedtakstatistikkTask.TYPE)
)

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this.type == it.first.type }.second
fun TaskType.nestePubliseringsflytTask() = publiseringsflyt().zipWithNext().first { this.type == it.first.type }.second


fun Task.opprettNesteTask(): Task {
    val nesteTask = TaskType(this.type).nesteHovedflytTask()
    return lagTask(nesteTask)
}

fun Task.opprettNestePubliseringTask(): Task {
    val nesteTask = TaskType(this.type).nestePubliseringsflytTask()
    return lagTask(nesteTask)
}

private fun Task.lagTask(nesteTask: TaskType): Task {
    return if (nesteTask.triggerTidAntallSekunderFrem != null) {
        Task(type = nesteTask.type,
             payload = this.payload,
             properties = this.metadata).copy(triggerTid = LocalDateTime.now()
                .plusSeconds(nesteTask.triggerTidAntallSekunderFrem))

    } else {
        Task(type = nesteTask.type,
             payload = this.payload,
             properties = this.metadata)
    }
}
