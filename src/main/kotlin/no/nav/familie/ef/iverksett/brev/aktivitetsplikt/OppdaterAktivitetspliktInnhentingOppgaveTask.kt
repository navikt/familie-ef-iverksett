package no.nav.familie.ef.iverksett.brev.aktivitetsplikt

import no.nav.familie.ef.iverksett.felles.util.dagensDatoMedTidNorskFormat
import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OppdaterAktivitetspliktInnhentingOppgaveTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Oppdaterer beskrivelse på oppgave etter distribusjon av brev for innhenting av aktivitetsplikt.",
)
class OppdaterAktivitetspliktInnhentingOppgaveTask(
    private val aktivitetspliktBrevRepository: AktivitetspliktBrevRepository,
    private val oppgaveService: OppgaveService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val brevId = UUID.fromString(task.payload)
        val brev = aktivitetspliktBrevRepository.findByIdOrThrow(brevId)
        val oppgave = oppgaveService.hentOppgave(brev.oppgaveId)

        val nyBeskrivelse = utledBeskrivelseForAktivitetspliktOppgave(oppgave.beskrivelse)
        val nyPrioritet = OppgavePrioritet.NORM
        val nyFrist = FRIST_OPPFØLGINGSOPPGAVE

        validerOppgave(oppgave)

        oppgaveService.oppdaterOppgave(
            Oppgave(
                id = brev.oppgaveId,
                beskrivelse = nyBeskrivelse,
                prioritet = nyPrioritet,
                fristFerdigstillelse = nyFrist,
            ),
        )
    }

    private fun validerOppgave(
        oppgave: Oppgave,
    ) {
        if (oppgave.fristFerdigstillelse != FRIST_OPPRINNELIG_OPPGAVE) {
            throw IllegalStateException(
                "Kan ikke oppdatere verdier på oppgave med id=${oppgave.id}. Oppgaven har blitt endret på underveis i flyten for innhenting av aktivitetsplikt.",
            )
        }
    }

    companion object {
        const val TYPE = "OppdaterAktivitetspliktInnhentingOppgaveTask"
        const val FRIST_OPPFØLGINGSOPPGAVE = "2025-07-27"
        const val FRIST_OPPRINNELIG_OPPGAVE = "2025-05-17"

        fun utledBeskrivelseForAktivitetspliktOppgave(oppgaveBeskrivelse: String?): String {
            val tidligereBeskrivelse = "\n${oppgaveBeskrivelse.orEmpty()}"
            val prefix = "--- ${dagensDatoMedTidNorskFormat()} Utført av familie-ef-sak ---\n"
            val nyttBeskrivelseInnslag = "Brev om innhenting av dokumentasjon på videre aktivitet er sendt til bruker.\n"
            val nyBeskrivelse = prefix + nyttBeskrivelseInnslag + tidligereBeskrivelse

            return nyBeskrivelse.trimEnd()
        }
    }
}
