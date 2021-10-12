package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
        taskStepType = OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad.TYPE,
        beskrivelse = "Oppretter oppgave om at bruker har innvilget overgangsstønad"
)
class OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad(
        private val oppgaveService: OppgaveService,
        private val iverksettingRepository: IverksettingRepository,
        private val taskRepository: TaskRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val iverksett = iverksettingRepository.hent(UUID.fromString(task.payload))

        oppgaveService.opprettVurderHendelseOppgave(iverksett)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "opprettOppfølgingOppgaveForInnvilgetOvergangsstønad"
    }
}
