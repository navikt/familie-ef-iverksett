package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
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
    taskStepType = VedtakstatistikkTask.TYPE,
    beskrivelse = "Sender vedtaksstatistikk til DVH.",
    settTilManuellOppfølgning = true,
)
class VedtakstatistikkTask(
    private val iverksettingRepository: IverksettingRepository,
    private val vedtakstatistikkService: VedtakstatistikkService,
    private val taskService: TaskService,
    private val featureToggleService: FeatureToggleService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val forrigeIverksett =
            iverksett.behandling.forrigeBehandlingId
                ?.let { iverksettingRepository.findByIdOrThrow(it) }
                ?.data
        vedtakstatistikkService.sendTilKafka(iverksett, forrigeIverksett)
    }

    override fun onCompletion(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        if (iverksett.erGOmregning() && featureToggleService.isEnabled("familie.ef.sak.g-beregning-scheduler")) {
            taskService.save(task.opprettNestePubliseringTask())
        }
    }

    companion object {
        const val TYPE = "sendVedtakstatistikk"
    }
}
