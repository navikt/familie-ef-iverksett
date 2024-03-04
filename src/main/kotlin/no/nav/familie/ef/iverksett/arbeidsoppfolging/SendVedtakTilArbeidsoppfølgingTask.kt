package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendVedtakTilArbeidsoppfølgingTask.TYPE,
    beskrivelse = "Sender vedtaksperioder til arbeidsoppfølging.",
    settTilManuellOppfølgning = true,
)
class SendVedtakTilArbeidsoppfølgingTask(
    private val taskService: TaskService,
    private val iverksettingRepository: IverksettingRepository,
    private val arbeidsoppfølgingService: ArbeidsoppfølgingService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        arbeidsoppfølgingService.sendTilKafka(iverksett)
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {
        const val TYPE = "sendVedtakTilArbeidsoppfølging"
    }
}
