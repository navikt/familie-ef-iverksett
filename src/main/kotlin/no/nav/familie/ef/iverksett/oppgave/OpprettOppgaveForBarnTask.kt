package no.nav.familie.ef.iverksett.oppgave

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForBarn
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
        taskStepType = OpprettOppgaveForBarnTask.TYPE,
        beskrivelse = "Oppretter oppgave for barn som fyller 1/2 eller 1 år")
class OpprettOppgaveForBarnTask(val taskRepository: TaskRepository,
                                val opprettOppgaveForBarnService: OpprettOppgaverForBarnService,
                                val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val oppgaveForBarn = objectMapper.readValue<OppgaveForBarn>(task.payload)
        opprettOppgaveForBarnService.opprettOppgaveForBarnSomFyllerAar(oppgaveForBarn)
    }

    companion object {

        const val TYPE = "opprettOppgaveForBarnTask"
    }
}