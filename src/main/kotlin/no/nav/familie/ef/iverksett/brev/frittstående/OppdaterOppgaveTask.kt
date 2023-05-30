package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OppdaterOppgaveTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Oppdaterer beskrivelse på oppgave etter distribusjon av brev for innhenting av karakterutskrift.",
)
class OppdaterOppgaveTask(
    private val karakterutskriftBrevRepository: KarakterutskriftBrevRepository,
    private val oppgaveService: OppgaveService,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val brevId = UUID.fromString(task.payload)
        val brev = karakterutskriftBrevRepository.findByIdOrThrow(brevId)
        val oppgave = oppgaveService.hentOppgave(brev.oppgaveId)

        val beskrivelse = OppgaveUtil.utledBeskrivelseForKarakterinnhentingOppgave(oppgave.beskrivelse)
        val prioritet = OppgaveUtil.utledPrioritetForKarakterinnhentingOppgave(oppgave.fristFerdigstillelse, oppgave.id)
        val frist = OppgaveUtil.utledFristForKarakterinnhentingOppgave(oppgave.fristFerdigstillelse, oppgave.id)

        oppgaveService.oppdaterOppgave(
            Oppgave(
                id = brev.oppgaveId,
                beskrivelse = beskrivelse,
                prioritet = prioritet,
                fristFerdigstillelse = frist.toString(),
            ),
        )
    }

    companion object {
        const val TYPE = "OppdaterOppgaveTask"
    }
}
