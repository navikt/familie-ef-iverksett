package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.Locale

object OppgaveUtil {

    private val fristHovedperiodeForInnhentingAvKarakterutskrift: LocalDate = LocalDate.parse("2023-05-17")
    private val fristutvidetForInnhentingAvKarakterutskrift = LocalDate.parse("2023-05-18")

    fun opprettBehandlingstema(stønadstype: StønadType): Behandlingstema {
        return Behandlingstema
            .fromValue(
                stønadstype.name.lowercase(Locale.getDefault())
                    .replaceFirstChar { it.uppercase() },
            )
    }

    fun opprettOppgaveRequest(
        eksternFagsakId: Long,
        personIdent: String,
        stønadstype: StønadType,
        enhetId: String,
        oppgavetype: Oppgavetype,
        beskrivelse: String,
        settBehandlesAvApplikasjon: Boolean,
        fristFerdigstillelse: LocalDate? = null,
        mappeId: Long? = null,
    ): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
            ident = OppgaveIdentV2(ident = personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
            saksId = eksternFagsakId.toString(),
            tema = Tema.ENF,
            oppgavetype = oppgavetype,
            fristFerdigstillelse = fristFerdigstillelse(fristFerdigstillelse),
            beskrivelse = beskrivelse,
            enhetsnummer = enhetId,
            behandlingstema = opprettBehandlingstema(stønadstype).value,
            tilordnetRessurs = null,
            behandlesAvApplikasjon = if (settBehandlesAvApplikasjon) "familie-ef-sak" else null,
            mappeId = mappeId,
        )
    }

    fun utledPrioritetForKarakterinnhentingOppgave(oppgaveFrist: String?, oppgaveId: Long?) = when (LocalDate.parse(oppgaveFrist)) {
        fristHovedperiodeForInnhentingAvKarakterutskrift -> OppgavePrioritet.NORM
        fristutvidetForInnhentingAvKarakterutskrift -> OppgavePrioritet.LAV
        else -> throw IllegalStateException("Kan ikke oppdatere prioritet på oppgave=$oppgaveId")
    }

    fun utledBeskrivelseForKarakterinnhentingOppgave(oppgaveBeskrivelse: String?): String {
        val tidligereBeskrivelse = "\n${oppgaveBeskrivelse.orEmpty()}"
        val nyttBeskrivelsesInnslag = "Brev om innhenting av karakterutskrift er sendt ut.\n"
        val nyBeskrivelse = nyttBeskrivelsesInnslag + tidligereBeskrivelse

        return nyBeskrivelse.trimEnd()
    }

    fun utledFristForKarakterinnhentingOppgave(oppgaveFrist: String?, oppgaveId: Long?) = when (LocalDate.parse(oppgaveFrist)) {
        fristHovedperiodeForInnhentingAvKarakterutskrift -> LocalDate.of(2023, 8, 5)
        fristutvidetForInnhentingAvKarakterutskrift -> LocalDate.of(2023, 8, 6)
        else -> throw IllegalStateException("Kan ikke oppdatere frist på oppgave=$oppgaveId")
    }

    private fun fristFerdigstillelse(aktivFra: LocalDate?, daysToAdd: Long = 0): LocalDate {
        var date = (aktivFra?.atTime(LocalTime.now()) ?: LocalDateTime.now()).plusDays(daysToAdd)

        if (date.hour >= 14) {
            date = date.plusDays(1)
        }

        when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date = date.plusDays(2)
            DayOfWeek.SUNDAY -> date = date.plusDays(1)
            else -> {
            }
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
            else -> {
            }
        }

        return date.toLocalDate()
    }
}
