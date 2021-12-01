package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppfølgingsOppgaveTask.TYPE,
    beskrivelse = "Oppretter oppgave om at bruker har innvilget overgangsstønad"
)
class OpprettOppfølgingsOppgaveTask(
    private val oppgaveService: OppgaveService,
    private val iverksettingRepository: IverksettingRepository,
    private val taskRepository: TaskRepository,
    private val featureToggleService: FeatureToggleService
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        if (!featureToggleService.isEnabled("familie.ef.iverksett.skip-opprett-oppfoelgningsoppgave")) {
            logger.warn("Oppretter ikke oppfølgningsoppgave for ${task.payload} pga disablet feature toggle")
            return
        }
        val iverksett = iverksettingRepository.hent(UUID.fromString(task.payload))

        if (oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)) {
            val oppgaveId = oppgaveService.opprettVurderHendelseOppgave(iverksett)
            logger.info("Opprettet oppgave for oppgaveID=${oppgaveId}")
        }
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "opprettOppfølgingOppgaveForInnvilgetOvergangsstønad"
    }
}
