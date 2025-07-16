package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.ef.iverksett.arbeidsoppfolging.SendVedtakTilArbeidsoppfølgingTask
import no.nav.familie.ef.iverksett.arena.SendFattetVedtakTilArenaTask
import no.nav.familie.ef.iverksett.behandlingsstatistikk.BehandlingsstatistikkTask
import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.brukernotifikasjon.SendBrukernotifikasjonVedGOmregningTask
import no.nav.familie.ef.iverksett.infotrygd.SendPerioderTilInfotrygdTask
import no.nav.familie.ef.iverksett.oppgave.OpprettFremleggsoppgaverTask
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingsOppgaveForOvergangsstønadTask
import no.nav.familie.ef.iverksett.tilbakekreving.OpprettTilbakekrevingTask
import no.nav.familie.ef.iverksett.vedtak.PubliserVedtakTilKafkaTask
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkTask
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.ef.iverksett.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import java.time.LocalDateTime

class TaskType(
    val type: String,
    val triggerTidAntallSekunderFrem: Long? = null,
)

fun hovedflyt() =
    listOf(
        TaskType(OpprettTilbakekrevingTask.TYPE),
        TaskType(IverksettMotOppdragTask.TYPE),
        TaskType(VentePåStatusFraØkonomiTask.TYPE, 20), // går ikke videre ved migrering//korrigering_uten_brev
        TaskType(JournalførVedtaksbrevTask.TYPE),
        TaskType(DistribuerVedtaksbrevTask.TYPE),
    )

fun publiseringsflyt() =
    listOf(
        TaskType(SendPerioderTilInfotrygdTask.TYPE), // Hopper til vedtakstatistikk ved migrering
        TaskType(SendFattetVedtakTilArenaTask.TYPE),
        TaskType(PubliserVedtakTilKafkaTask.TYPE),
        TaskType(SendVedtakTilArbeidsoppfølgingTask.TYPE),
        TaskType(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE),
        TaskType(OpprettFremleggsoppgaverTask.TYPE),
        TaskType(VedtakstatistikkTask.TYPE),
        TaskType(SendBrukernotifikasjonVedGOmregningTask.TYPE),
        TaskType(BehandlingsstatistikkTask.TYPE),
    )

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this.type == it.first.type }.second

fun TaskType.nestePubliseringsflytTask() = publiseringsflyt().zipWithNext().first { this.type == it.first.type }.second

fun Task.opprettNesteTask(): Task {
    val nesteTask = TaskType(this.type).nesteHovedflytTask()
    return lagTask(nesteTask)
}

fun Task.opprettNestePubliseringTask(erMigrering: Boolean = false): Task {
    val nesteTask =
        if (erMigrering && this.type == SendPerioderTilInfotrygdTask.TYPE) {
            TaskType(VedtakstatistikkTask.TYPE)
        } else {
            TaskType(this.type).nestePubliseringsflytTask()
        }
    return lagTask(nesteTask)
}

private fun Task.lagTask(nesteTask: TaskType): Task =
    if (nesteTask.triggerTidAntallSekunderFrem != null) {
        Task(
            type = nesteTask.type,
            payload = this.payload,
            properties = this.metadata,
        ).copy(
            triggerTid =
                LocalDateTime
                    .now()
                    .plusSeconds(nesteTask.triggerTidAntallSekunderFrem),
        )
    } else {
        Task(
            type = nesteTask.type,
            payload = this.payload,
            properties = this.metadata,
        )
    }
