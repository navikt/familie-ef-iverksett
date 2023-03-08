package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE,
    beskrivelse = "Oppretter oppgave om at bruker har innvilget overgangsstønad",
)
class OpprettOppfølgingsOppgaveForOvergangsstønadTask(
    private val oppgaveService: OppgaveService,
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)
        if (iverksett.data !is IverksettOvergangsstønad) {
            logger.info(
                "Oppretter ikke oppfølgningsoppgave for behandling=$behandlingId" +
                    " da det ikke er en overgangsstønad (${iverksett::class.java.simpleName})",
            )
            return
        }

        if (oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett.data)) {
            val oppgaveId = oppgaveService.opprettOppgave(
                iverksett.data,
                Oppgavetype.VurderHenvendelse,
                oppgaveService.lagOppgavebeskrivelse(iverksett.data)
            )
            logger.info("Opprettet oppgave for behandling=$behandlingId oppgave=$oppgaveId")
        }
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "opprettOppfølgingOppgaveForInnvilgetOvergangsstønad"
    }
}
