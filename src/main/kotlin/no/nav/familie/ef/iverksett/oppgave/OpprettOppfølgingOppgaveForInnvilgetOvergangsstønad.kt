package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad.TYPE,
    beskrivelse = "Oppretter oppgave om at bruker har innvilget overgangsstønad"
)
class OpprettOppfølgingOppgaveForInnvilgetOvergangsstønad(
    val oppgaveClient: OppgaveClient,
    val iverksettingRepository: IverksettingRepository,
    val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    val taskRepository: TaskRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val iverksett = iverksettingRepository.hent(UUID.fromString(task.payload))
        val enhetsnummer = familieIntegrasjonerClient.hentNavEnhetForOppfølging(iverksett.søker.personIdent)
        val opprettOppgaveRequest =
            OpprettOppgaveRequest(
                ident = OppgaveIdentV2(ident = iverksett.søker.personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
                saksId = iverksett.fagsak.eksternId.toString(),
                tema = Tema.ENF,
                oppgavetype = Oppgavetype.VurderHenvendelse,
                fristFerdigstillelse = fristFerdigstillelse(),
                beskrivelse = oppgaveBeskrivelse(iverksett),
                enhetsnummer = enhetsnummer?.enhetId,
                behandlingstema = Behandlingstema.fromValue(iverksett.fagsak.stønadstype.name.toLowerCase().capitalize()).value,
                tilordnetRessurs = null,
                behandlesAvApplikasjon = "familie-ef-sak"
            )

        oppgaveClient.opprettOppgave(opprettOppgaveRequest)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    private fun oppgaveBeskrivelse(iverksett: Iverksett): String {
        val gjeldendeVedtak = iverksett.vedtak.vedtaksperioder.sortedBy { it.fraOgMed }.last()
        return "${iverksett.fagsak.stønadstype.name.enumToReadable()} er innvilget fra " +
                "${gjeldendeVedtak.fraOgMed.toReadable()} - ${gjeldendeVedtak.tilOgMed.toReadable()}. " +
                "Aktivitetsplikt: ${gjeldendeVedtak.aktivitet.name.enumToReadable()}" +
                ". Periodetype: ${gjeldendeVedtak.periodeType.name.enumToReadable()}. Saken ligger i ny løsning."
    }


    companion object {
        const val TYPE = "opprettOppfølgingOppgaveForInnvilgetOvergangsstønad"
    }

    fun String.enumToReadable(): String {
        return this.replace("_", " ").toLowerCase().capitalize()
    }

    fun LocalDate.toReadable(): String {
        return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

}

fun fristFerdigstillelse(daysToAdd: Long = 0): LocalDate {
    var date = LocalDateTime.now().plusDays(daysToAdd)

    if (date.hour >= 14) {
        date = date.plusDays(1)
    }

    when (date.dayOfWeek) {
        DayOfWeek.SATURDAY -> date = date.plusDays(2)
        DayOfWeek.SUNDAY -> date = date.plusDays(1)
    }

    when {
        date.dayOfMonth == 1 && date.month == Month.JANUARY -> date = date.plusDays(1)
        date.dayOfMonth == 1 && date.month == Month.MAY -> date = date.plusDays(1)
        date.dayOfMonth == 17 && date.month == Month.MAY -> date = date.plusDays(1)
        date.dayOfMonth == 25 && date.month == Month.DECEMBER -> date = date.plusDays(2)
        date.dayOfMonth == 26 && date.month == Month.DECEMBER -> date = date.plusDays(1)
    }

    when (date.dayOfWeek) {
        DayOfWeek.SATURDAY -> date = date.plusDays(2)
        DayOfWeek.SUNDAY -> date = date.plusDays(1)
    }

    return date.toLocalDate()
}
