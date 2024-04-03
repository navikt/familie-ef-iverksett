package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = BehandlingsstatistikkTask.TYPE,
    beskrivelse = "Sender melding til behandlingsstatistikk om at behandling er G-omregnet",
    settTilManuellOppfølgning = true,
)
class BehandlingsstatistikkTask(
    val behandlingsstatistikkService: BehandlingsstatistikkService,
    val behandlingsstatistikkProducer: BehandlingsstatistikkProducer,
    val iverksettingRepository: IverksettingRepository,
    val featureToggleService: FeatureToggleService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        if (iverksett is IverksettOvergangsstønad &&
            iverksett.erGOmregning() &&
            featureToggleService.isEnabled("familie.ef.sak.g-beregning-scheduler")
        ) {
            val behandlingDVH = behandlingsstatistikkService.mapGOmregningIverksettingTilBehandlingDVH(iverksett)
            behandlingsstatistikkProducer.sendBehandling(behandlingDVH)
        }
    }

    companion object {
        const val TYPE = "BehandlingsstatistikkTask"
    }
}
