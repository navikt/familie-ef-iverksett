package no.nav.familie.ef.iverksett.oppgave.terminbarn

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
        taskStepType = OpprettOppgaverTerminbarnTask.TYPE,
        beskrivelse = "Oppretter oppgave for terminbarn som ikke er født")
class OpprettOppgaverTerminbarnTask(val taskRepository: TaskRepository,
                                    val opprettOppgaverForTerminbarnService: OpprettOppgaverTerminbarnService,
                                    val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        if (featureToggleService.isEnabled("familie.ef.iverksett.opprett-oppgaver-ugyldigeterminbarn")) {
            val oppgaveForBarn = objectMapper.readValue<OppgaveForBarn>(task.payload)
            opprettOppgaverForTerminbarnService.opprettOppgaveForTerminbarn(oppgaveForBarn)
        } else {
            logger.warn("Feature toggle opprett-oppgaver-ugyldigeterminbarn er ikke enablet. TaskID=${task.id}")
        }
    }

    companion object {

        const val TYPE = "opprettOppgaverTerminbarnTask"
    }
}