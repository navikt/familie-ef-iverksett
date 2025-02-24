package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.kontrakter.ef.iverksett.OppgaveForOpprettelseType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class OppgaveUtilTest {
    @Test
    fun `fristFerdigstillelse skal alltid være 15 desember når det er inntektskontroll av selvstendig næringsdrivende`() {
        val femtendeDesemberNesteÅr =
            LocalDate
                .now()
                .plusYears(1)
                .withMonth(Month.DECEMBER.value)
                .withDayOfMonth(15)
        val request = lagRequest(femtendeDesemberNesteÅr, OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE)
        assertThat(request.fristFerdigstillelse).isEqualTo(femtendeDesemberNesteÅr)
    }

    @Test
    fun `fristFerdigstillelse skal ikke endres når det er inntektskontroll av selvstendig næringsdrivende`() {
        val den17MaiNesteÅr =
            LocalDate
                .now()
                .plusYears(1)
                .withMonth(Month.MAY.value)
                .withDayOfMonth(17)
        val request = lagRequest(den17MaiNesteÅr, OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE)
        assertThat(request.fristFerdigstillelse).isEqualTo(den17MaiNesteÅr)
    }

    @Test
    fun `fristFerdigstillelse skal endres når den lander på 17 mai or det er  inntektskontroll ett år frem i tid`() {
        val den17MaiNesteÅr =
            LocalDate
                .now()
                .plusYears(1)
                .withMonth(Month.MAY.value)
                .withDayOfMonth(17)
        val request = lagRequest(den17MaiNesteÅr, OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID)
        assertThat(request.fristFerdigstillelse).isNotEqualTo(den17MaiNesteÅr)
    }

    private fun lagRequest(
        fristFerdigstillelse: LocalDate,
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
    ) = OppgaveUtil.opprettOppgaveRequest(
        eksternFagsakId = 1L,
        personIdent = "12345678910",
        stønadstype = StønadType.OVERGANGSSTØNAD,
        enhetId = "enhetId",
        oppgavetype = Oppgavetype.Fremlegg,
        beskrivelse = "Test",
        settBehandlesAvApplikasjon = true,
        fristFerdigstillelse = fristFerdigstillelse,
        oppgaveForOpprettelseType = oppgaveForOpprettelseType,
    )
}
