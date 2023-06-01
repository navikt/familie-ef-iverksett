package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.opprettBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.util.UUID

class OppdaterOppgaveTaskTest {

    private val karakterutskriftBrevRepository = mockk<KarakterutskriftBrevRepository>()
    private val oppgaveService = mockk<OppgaveService>()

    private val fristHovedperiode = "2023-05-17"
    private val fristUtvidet = "2023-05-18"
    private val hovedPeriode = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE

    private val oppdaterOppgaveTask = OppdaterOppgaveTask(karakterutskriftBrevRepository, oppgaveService)
    private val oppgaveSlot = slot<Oppgave>()

    @BeforeEach
    fun setUp() {
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns opprettBrev(hovedPeriode)
        every { oppgaveService.oppdaterOppgave(capture(oppgaveSlot)) } returns 1L
    }

    @Test
    fun `skal oppdatere oppgave med tidligere beskrivelse og frist hovedperiode`() {
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "Oppgave opprettet.", frist = fristHovedperiode)
        oppdaterOppgaveTask.doTask(Task(OppdaterOppgaveTask.TYPE, UUID.randomUUID().toString()))

        val oppdaterteVerdier = oppgaveSlot.captured

        verify(exactly = 1) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(oppdaterteVerdier.id).isEqualTo(5L)
        assertThat(oppdaterteVerdier.beskrivelse).isEqualTo(nyBeskrivelse)
        assertThat(oppdaterteVerdier.prioritet).isEqualTo(OppgavePrioritet.NORM)
        assertThat(oppdaterteVerdier.fristFerdigstillelse).isEqualTo("2023-08-05")
    }

    @Test
    fun `skal oppdatere oppgave uten tidligere beskrivelse og frist utvidet periode`() {
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = fristUtvidet)
        oppdaterOppgaveTask.doTask(Task(OppdaterOppgaveTask.TYPE, UUID.randomUUID().toString()))

        val oppdaterteVerdier = oppgaveSlot.captured

        verify(exactly = 1) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(oppdaterteVerdier.id).isEqualTo(5L)
        assertThat(oppdaterteVerdier.beskrivelse).isEqualTo("Brev om innhenting av karakterutskrift er sendt ut.")
        assertThat(oppdaterteVerdier.prioritet).isEqualTo(OppgavePrioritet.LAV)
        assertThat(oppdaterteVerdier.fristFerdigstillelse).isEqualTo("2023-08-06")
    }

    @Test
    fun `skal kaste feil dersom fristen hverken er hovedperiode eller utvidet periode`() {
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = "2023-05-10")

        val feil = assertThrows<IllegalStateException> {
            oppdaterOppgaveTask.doTask(
                Task(
                    OppdaterOppgaveTask.TYPE,
                    UUID.randomUUID().toString(),
                ),
            )
        }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Kan ikke oppdatere prioritet på oppgave=5")
    }

    private val nyBeskrivelse = "Brev om innhenting av karakterutskrift er sendt ut.\n\nOppgave opprettet."

    private fun oppgave(beskrivelse: String, frist: String) =
        Oppgave(id = 5L, beskrivelse = beskrivelse, fristFerdigstillelse = frist)
}
