package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@TaskStepBeskrivelse(
        taskStepType = OpprettOppgaveForBarnTask.TYPE,
        beskrivelse = "Oppretter oppgave for barn som fyller 1/2 eller 1 år",

        )
class OpprettOppgaveForBarnTask(val taskRepository: TaskRepository,
                                val opprettOppgaveForBarnService: OpprettOppgaveForBarnService,
                                val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    val DATE_FORMAT_ISO_YEAR_MONTH_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val INNEN_ANTALL_UKER = 1L

    override fun doTask(task: Task) {
        if (featureToggleService.isEnabled("familie.ef.iverksett.opprett-oppgaver-barnsomfylleraar")) {
            /** TODO : Sende med dato for siste kjøring */
            opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(INNEN_ANTALL_UKER, LocalDate.now().minusWeeks(1))
        }
    }

    override fun onCompletion(task: Task) {
        opprettTaskForNesteUke()
    }

    fun opprettTaskForNesteUke() {
        val nesteUke = LocalDate.now().plusWeeks(1)
        val triggerTid = nesteUke.atTime(5, 0)
        taskRepository.save(Task(TYPE, nesteUke.format(DATE_FORMAT_ISO_YEAR_MONTH_DAY)).medTriggerTid(triggerTid))
    }

    companion object {

        const val TYPE = "opprettOppgaveForBarnTask"
    }
}