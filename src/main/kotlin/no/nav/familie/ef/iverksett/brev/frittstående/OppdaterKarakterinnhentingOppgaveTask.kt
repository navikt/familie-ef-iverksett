package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OppdaterKarakterinnhentingOppgaveTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Oppdaterer beskrivelse på oppgave etter distribusjon av brev for innhenting av karakterutskrift.",
)
class OppdaterKarakterinnhentingOppgaveTask(
    private val karakterutskriftBrevRepository: KarakterutskriftBrevRepository,
    private val oppgaveService: OppgaveService,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val brevId = UUID.fromString(task.payload)
        val brev = karakterutskriftBrevRepository.findByIdOrThrow(brevId)
        val oppgave = oppgaveService.hentOppgave(brev.oppgaveId)

        val beskrivelse = utledBeskrivelseForKarakterinnhentingOppgave(oppgave.beskrivelse)
        val prioritet = utledPrioritetForKarakterinnhentingOppgave(oppgave.fristFerdigstillelse, oppgave.id)
        val frist = utledFristForKarakterinnhentingOppgave(oppgave.fristFerdigstillelse, oppgave.id)

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
        const val TYPE = "OppdaterKarakterinnhentingOppgaveTask"

        val fristHovedperiodeForInnhentingAvKarakterutskrift: LocalDate = LocalDate.parse("2023-05-17")
        val fristutvidetForInnhentingAvKarakterutskrift = LocalDate.parse("2023-05-18")

        fun utledPrioritetForKarakterinnhentingOppgave(oppgaveFrist: String?, oppgaveId: Long?) = when (LocalDate.parse(oppgaveFrist)) {
            fristHovedperiodeForInnhentingAvKarakterutskrift -> OppgavePrioritet.NORM
            fristutvidetForInnhentingAvKarakterutskrift -> OppgavePrioritet.LAV
            else -> throw IllegalStateException("Kan ikke oppdatere prioritet på oppgave=$oppgaveId")
        }

        fun utledBeskrivelseForKarakterinnhentingOppgave(oppgaveBeskrivelse: String?): String {
            val tidligereBeskrivelse = "\n${oppgaveBeskrivelse.orEmpty()}"
            val nyttBeskrivelseInnslag = "Brev om innhenting av karakterutskrift er sendt ut.\n"
            val nyBeskrivelse = nyttBeskrivelseInnslag + tidligereBeskrivelse

            return nyBeskrivelse.trimEnd()
        }

        fun utledFristForKarakterinnhentingOppgave(oppgaveFrist: String?, oppgaveId: Long?) = when (LocalDate.parse(oppgaveFrist)) {
            fristHovedperiodeForInnhentingAvKarakterutskrift -> LocalDate.of(2023, 8, 5)
            fristutvidetForInnhentingAvKarakterutskrift -> LocalDate.of(2023, 8, 6)
            else -> throw IllegalStateException("Kan ikke oppdatere frist på oppgave=$oppgaveId")
        }
    }
}
