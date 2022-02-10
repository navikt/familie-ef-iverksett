package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.arbeidsoppfølging.VedtakArbeidsoppfølging
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.Locale


object OppgaveUtil {

    fun opprettBehandlingstema(stønadstype: StønadType): Behandlingstema {
        return Behandlingstema
                .fromValue(stønadstype.name.lowercase(Locale.getDefault())
                                   .replaceFirstChar { it.uppercase() })
    }

    fun opprettOppgaveRequest(eksternFagsakId: Long,
                              personIdent: String,
                              stønadstype: StønadType,
                              enhetsnummer: Enhet,
                              oppgavetype: Oppgavetype,
                              beskrivelse: String): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
                ident = OppgaveIdentV2(ident = personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
                saksId = eksternFagsakId.toString(),
                tema = Tema.ENF,
                oppgavetype = oppgavetype,
                fristFerdigstillelse = fristFerdigstillelse(),
                beskrivelse = beskrivelse,
                enhetsnummer = enhetsnummer.enhetId,
                behandlingstema = opprettBehandlingstema(stønadstype).value,
                tilordnetRessurs = null,
                behandlesAvApplikasjon = "familie-ef-sak"
        )
    }

    fun opprettVedtakArbeidsoppfølging(iverksett: Iverksett, enhet: Enhet, beskrivelse: String): VedtakArbeidsoppfølging {
        return VedtakArbeidsoppfølging(iverksett.behandling.eksternId,
                                       iverksett.søker.personIdent,
                                       iverksett.fagsak.stønadstype,
                                       enhet,
                                       Oppgavetype.VurderHenvendelse,
                                       iverksett.vedtak.vedtaksperioder,
                                       beskrivelse)
    }

    private fun fristFerdigstillelse(daysToAdd: Long = 0): LocalDate {
        var date = LocalDateTime.now().plusDays(daysToAdd)

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
