package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForOpprettelseType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettFremleggsoppgaveForOvergangsstønadTask.TYPE,
    beskrivelse = "Oppretter fremleggsoppgave om kontroll av inntekt",
)
class OpprettFremleggsoppgaveForOvergangsstønadTask(
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
                "Oppretter ikke fremleggsoppgave for behandling=$behandlingId" +
                    " da det ikke er en overgangsstønad (${iverksett::class.java.simpleName})",
            )
            return
        }
        if (iverksett.data.vedtak.oppgaverForOpprettelse.oppgavetyper.contains(
                OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID,
            )
        ) {
            val oppgaveId = oppgaveService.opprettFremleggsoppgave(iverksett.data, "Inntekt")
            logger.info("Opprettet oppgave for behandling=$behandlingId oppgave=$oppgaveId")
        } else {
            logger.info("Oppgave opprettes ikke for behandling=$behandlingId")
        }
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {
        const val TYPE = "opprettFremleggsoppgaveForOvergangsstønad"
    }
}
