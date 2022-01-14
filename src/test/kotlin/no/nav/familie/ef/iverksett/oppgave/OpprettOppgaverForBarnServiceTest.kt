package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OpprettOppgaverForBarnServiceTest {

    val iverksett = mockk<Iverksett>()
    val oppgaveClient = mockk<OppgaveClient>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val taskRepository = mockk<TaskRepository>()
    val opprettOppgaveForBarnService =
            OpprettOppgaverForBarnService(oppgaveClient, familieIntegrasjonerClient, taskRepository)

    @BeforeEach
    fun init() {
        mockkObject(OppgaveUtil)
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { iverksett.søker.personIdent } returns "1234567890"
        every { iverksett.behandling.behandlingId } returns UUID.randomUUID()
        every { familieIntegrasjonerClient.hentAktørId(any()) } returns ""
        every { OppgaveUtil.opprettOppgaveRequest(any(), any(), any(), any(), any(), any()) } returns mockk()
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
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandlingMedRelasjoner(any()) } returns listOf(Enhet("", ""))
        opprettOppgaveForBarnService.opprettOppgaveForBarnSomFyllerAar(opprettOppgaveForBarn())
        verify(exactly = 1) { oppgaveClient.opprettOppgave(any()) }
    }

    private fun opprettOppgaveForBarn(id: UUID = UUID.randomUUID(), beskrivelse: String = "beskrivelse"): OppgaveForBarn {
        return OppgaveForBarn(id, 0L, "12345678910", StønadType.OVERGANGSSTØNAD.name, beskrivelse)
    }
}