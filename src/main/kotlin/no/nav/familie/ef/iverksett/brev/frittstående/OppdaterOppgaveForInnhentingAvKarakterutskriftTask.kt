package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service


@Service
@TaskStepBeskrivelse(
    taskStepType = DistribuerKarakterutskriftBrevTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Oppdaterer beskrivelse på oppgave for innhenting av karakterutskrift.",
)
class OppdaterOppgaveForInnhentingAvKarakterutskriftTask(
    private val taskService: TaskService,
) : AsyncTaskStep {


    override fun doTask(task: Task) {
        //TODO: Implementer
    }

    companion object {

        const val TYPE = "oppdaterOppgaveForInnhentingAvKarakterutskrift"
    }
}
