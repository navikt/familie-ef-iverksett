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
import java.util.*

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppgaveTask.TYPE,
    beskrivelse = "Oppretter oppgave om at bruker har innvilget overgangsstønad"
)
class OpprettOppgaveTask(
    val oppgaveClient: OppgaveClient,
    val iverksettingRepository: IverksettingRepository,
    val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    val taskRepository: TaskRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val iverksett = iverksettingRepository.hent(UUID.fromString(task.payload))
        val enhetsnummer = familieIntegrasjonerClient.hentNavEnhet(iverksett.søker.personIdent)
        val opprettOppgaveRequest =
            OpprettOppgaveRequest(
                ident = OppgaveIdentV2(ident = iverksett.søker.personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
                saksId = iverksett.fagsak.eksternId.toString(),
                tema = Tema.ENF,
                oppgavetype = Oppgavetype.VurderHenvendelse,
                fristFerdigstillelse = lagFristForOppgave(),
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

    private fun lagFristForOppgave(): LocalDate {
        val gjeldendeTid = LocalDateTime.now()
        val frist = when (gjeldendeTid.dayOfWeek) {
            DayOfWeek.FRIDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(2))
            DayOfWeek.SATURDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(2).withHour(8))
            DayOfWeek.SUNDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(1).withHour(8))
            else -> fristBasertPåKlokkeslett(gjeldendeTid)
        }

        return when (frist.dayOfWeek) {
            DayOfWeek.SATURDAY -> frist.plusDays(2)
            DayOfWeek.SUNDAY -> frist.plusDays(1)
            else -> frist
        }
    }

    private fun fristBasertPåKlokkeslett(gjeldendeTid: LocalDateTime): LocalDate {
        return if (gjeldendeTid.hour >= 12) {
            return gjeldendeTid.plusDays(2).toLocalDate()
        } else {
            gjeldendeTid.plusDays(1).toLocalDate()
        }
    }

    private fun oppgaveBeskrivelse(iverksett: Iverksett): String {
        val gjeldendeVedtak = iverksett.vedtak.vedtaksperioder.sortedBy { it.fraOgMed }.first()
        return "${iverksett.fagsak.stønadstype.name.enumToReadable()} er innvilget fra " +
                "${gjeldendeVedtak.fraOgMed} - ${gjeldendeVedtak.tilOgMed}. " +
                "Vedtaket er registrert med følgende aktivitetsplikt: " +
                gjeldendeVedtak.aktivitet.name.enumToReadable() +
                ". Med periodetype ${gjeldendeVedtak.periodeType.name.enumToReadable()}. Saken ligger i ny løsning."
    }

    companion object {
        const val TYPE = "sendPerioderTilInfotrygd"
    }

    fun String.enumToReadable(): String {
        return this.replace("_", " ").toLowerCase().capitalize()
    }
}