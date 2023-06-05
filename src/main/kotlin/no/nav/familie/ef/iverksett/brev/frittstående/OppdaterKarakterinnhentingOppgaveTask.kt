package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.ef.iverksett.felles.util.dagensDatoMedTidNorskFormat
import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
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

        validerOppgave(oppgave, brev)

        oppgaveService.oppdaterOppgave(
            Oppgave(
                id = brev.oppgaveId,
                beskrivelse = beskrivelse,
                prioritet = prioritet,
                fristFerdigstillelse = frist.toString(),
            ),
        )
    }

    private fun validerOppgave(oppgave: Oppgave, brev: KarakterutskriftBrev) {
        if (brev.brevtype == FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE && oppgave.fristFerdigstillelse != fristHovedperiodeString) {
            throw IllegalStateException("Kan ikke oppdatere verdier på oppgave med id=${oppgave.id}. Oppgaven har blitt endret på underveis i flyten for innhenting av karakterutskrift.")
        }
        if (brev.brevtype == FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_UTVIDET_PERIODE && oppgave.fristFerdigstillelse != fristUtvidetString) {
            throw IllegalStateException("Kan ikke oppdatere verdier på oppgave med id=${oppgave.id}. Oppgaven har blitt endret på underveis i flyten for innhenting av karakterutskrift.")
        }
    }

    companion object {

        const val TYPE = "OppdaterKarakterinnhentingOppgaveTask"

        val fristHovedperiodeString = "2023-05-17"
        val fristUtvidetString = "2023-05-18"
        val fristHovedperiode: LocalDate = LocalDate.parse(fristHovedperiodeString)
        val fristUtvidet = LocalDate.parse(fristUtvidetString)

        fun utledPrioritetForKarakterinnhentingOppgave(oppgaveFrist: String?, oppgaveId: Long?) =
            when (LocalDate.parse(oppgaveFrist)) {
                fristHovedperiode -> OppgavePrioritet.NORM
                fristUtvidet -> OppgavePrioritet.LAV
                else -> throw IllegalStateException("Kan ikke oppdatere prioritet på oppgave med id=$oppgaveId")
            }

        fun utledBeskrivelseForKarakterinnhentingOppgave(oppgaveBeskrivelse: String?): String {
            val tidligereBeskrivelse = "\n${oppgaveBeskrivelse.orEmpty()}"
            val prefix = "--- ${dagensDatoMedTidNorskFormat()} Utført av familie-ef-sak ---\n"
            val nyttBeskrivelseInnslag = "Brev om innhenting av karakterutskrift er sendt til bruker.\n"
            val nyBeskrivelse = prefix + nyttBeskrivelseInnslag + tidligereBeskrivelse

            return nyBeskrivelse.trimEnd()
        }

        fun utledFristForKarakterinnhentingOppgave(oppgaveFrist: String?, oppgaveId: Long?) =
            when (LocalDate.parse(oppgaveFrist)) {
                fristHovedperiode -> LocalDate.of(2023, 8, 5)
                fristUtvidet -> LocalDate.of(2023, 8, 6)
                else -> throw IllegalStateException("Kan ikke oppdatere frist på oppgave med id=$oppgaveId")
            }
    }
}
