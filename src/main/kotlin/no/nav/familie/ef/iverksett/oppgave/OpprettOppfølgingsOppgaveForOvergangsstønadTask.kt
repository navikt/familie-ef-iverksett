package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE,
    beskrivelse = "Oppretter oppgave om at bruker har innvilget overgangsstønad"
)
class OpprettOppfølgingsOppgaveForOvergangsstønadTask(
    private val oppgaveService: OppgaveService,
    private val iverksettingRepository: IverksettingRepository,
    private val taskRepository: TaskRepository
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        if (iverksett !is IverksettOvergangsstønad) {
            logger.info(
                "Oppretter ikke oppfølgningsoppgave for behandling=$behandlingId" +
                    " då den ikke er overgangsstønad (${iverksett::class.java.simpleName})"
            )
            return
        }

        if (oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)) {
            val oppgaveId = oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
            logger.info("Opprettet oppgave for behandling=$behandlingId oppgaveID=$oppgaveId")
        }
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "opprettOppfølgingOppgaveForInnvilgetOvergangsstønad"
    }
}
