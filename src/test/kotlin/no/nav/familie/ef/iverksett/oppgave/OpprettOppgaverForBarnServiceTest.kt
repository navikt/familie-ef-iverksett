package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForBarn
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class OpprettOppgaverForBarnServiceTest {

    val iverksett = mockk<Iverksett>()
    val oppgaveClient = mockk<OppgaveClient>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val taskRepository = mockk<TaskRepository>()
    val opprettOppgaveForBarnService =
            OpprettOppgaverForBarnService(oppgaveClient, familieIntegrasjonerClient, taskRepository)

    val oppgaveSlot = slot<OpprettOppgaveRequest>()

    @BeforeEach
    fun init() {
        oppgaveSlot.clear()
        every { oppgaveClient.opprettOppgave(capture(oppgaveSlot)) } returns 0L
        every { iverksett.søker.personIdent } returns "1234567890"
        every { iverksett.behandling.behandlingId } returns UUID.randomUUID()
        every { familieIntegrasjonerClient.hentAktørId(any()) } returns ""
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandlingMedRelasjoner(any()) } returns listOf(Enhet("", ""))
    }

    @Test
    fun `innhentDokumentasjon oppgave med samme beskrivelse finnes, forvent at oppgave ikke opprettes`() {
        every { oppgaveClient.hentOppgaver(any()) } returns listOf(Oppgave(oppgavetype = Oppgavetype.InnhentDokumentasjon.name,
                                                                           beskrivelse = "beskrivelse"))
        opprettOppgaveForBarnService.opprettOppgaveForBarnSomFyllerAar(opprettOppgaveForBarn())
        verify(exactly = 0) { oppgaveClient.opprettOppgave(any()) }
    }

    @Test
    fun `ingen oppgaver finnes for barn som fyller år, forvent at oppgave opprettes`() {
        every { oppgaveClient.hentOppgaver(any()) } returns emptyList()

        opprettOppgaveForBarnService.opprettOppgaveForBarnSomFyllerAar(opprettOppgaveForBarn())

        verify(exactly = 1) { oppgaveClient.opprettOppgave(any()) }
        val oppgaveRequest = oppgaveSlot.captured
        assertThat(oppgaveRequest.oppgavetype).isEqualTo(Oppgavetype.InnhentDokumentasjon)
        // pga att fristFerdigstillelse settes til annen dag hvis helg, etter 14 etc, så sjekker denne 1 uke frem i tiden
        assertThat(oppgaveRequest.fristFerdigstillelse).isBeforeOrEqualTo(LocalDate.now().plusWeeks(1))
    }

    @Test
    internal fun `skal lage fremleggningsoppgave med frist frem i tiden hvis aktivFra er satt`() {
        val aktivFra = LocalDate.now().plusYears(1)
        every { oppgaveClient.hentOppgaver(any()) } returns emptyList()

        opprettOppgaveForBarnService.opprettOppgaveForBarnSomFyllerAar(opprettOppgaveForBarn(aktivFra = aktivFra))

        verify(exactly = 1) { oppgaveClient.opprettOppgave(any()) }
        val oppgaveRequest = oppgaveSlot.captured
        assertThat(oppgaveRequest.oppgavetype).isEqualTo(Oppgavetype.Fremlegg)
        assertThat(oppgaveRequest.fristFerdigstillelse).isAfterOrEqualTo(aktivFra)
    }

    private fun opprettOppgaveForBarn(id: UUID = UUID.randomUUID(),
                                      beskrivelse: String = "beskrivelse",
                                      aktivFra: LocalDate? = null): OppgaveForBarn {
        return OppgaveForBarn(behandlingId = id,
                              eksternFagsakId = 0L,
                              personIdent = "12345678910",
                              stønadType = StønadType.OVERGANGSSTØNAD,
                              beskrivelse = beskrivelse, aktivFra = aktivFra)
    }
}