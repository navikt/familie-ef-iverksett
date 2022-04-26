package no.nav.familie.ef.iverksett.oppgave.barnsalder

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForBarn
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppgaveForBarnTask.TYPE,
    beskrivelse = "Oppretter oppgave for barn som fyller 1/2 eller 1 år")
class OpprettOppgaveForBarnTask(val taskRepository: TaskRepository,
                                val opprettOppgaveForBarnService: OpprettOppgaverForBarnService,
                                val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        if (featureToggleService.isEnabled("familie.ef.iverksett.opprett-oppgaver-barnsomfylleraar")) {
            val oppgaveForBarn = objectMapper.readValue<OppgaveForBarn>(task.payload)
            opprettOppgaveForBarnService.opprettOppgaveForBarnSomFyllerAar(oppgaveForBarn)
        } else {
            logger.warn("Feature toggle opprett-oppgaver-barnsomfylleraar er ikke enablet")
        }
    }

    companion object {

        const val TYPE = "opprettOppgaveForBarnTask"
    }
}