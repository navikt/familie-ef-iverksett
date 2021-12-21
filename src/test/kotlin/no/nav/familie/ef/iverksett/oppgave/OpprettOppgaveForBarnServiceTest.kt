package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.util.FnrGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Vi tar utgangspunkt i fra dato (men ikke fra og med), og til og med dato, ved sjekk om en fødselsdato slår til i løpet av
 * k antall uker.
 */
internal class OpprettOppgaveForBarnServiceTest {

    val iverksett = mockk<Iverksett>()
    val iverksettingRepository = mockk<IverksettingRepository>()
    val oppgaveClient = mockk<OppgaveClient>()
    val opprettOppgaveForBarnService = OpprettOppgaveForBarnService(oppgaveClient, iverksettingRepository)

    @BeforeEach
    fun init() {
        mockkObject(OppgaveUtil)
        mockkObject(OppgaveBeskrivelse)
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppgaveUtil.opprettOppgaveRequest(any(), any(), any()) } returns mockk()
        every { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() } returns ""
        every { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() } returns ""
    }

    @Test
    fun `barn blir seks mnd om 4 dager, sjekk om fyller innen 1 uke, forvent kall til beskrivelseBarnBlirSeksMnd`() {
        val fødselsdato = LocalDate.now().minusMonths(6).plusDays(3)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `barn blir seks mnd i dag, sjekk om fyller innen 1 uke, forvent ingen kall`() {
        val fødselsdato = LocalDate.now().minusMonths(6)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `barn blir seks mnd om 1 uker minus en dag, sjekk om fyller innen 1 uke, forvent kall til beskrivelseBarnBlirSeksMnd`() {
        val fødselsdato = LocalDate.now().minusMonths(6).plusWeeks(1).minusDays(1)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `barn blir seks mnd om 1 uke, sjekk om fyller innen 1 uke, forvent beskrivelseBarnBlirSeksMnd`() {
        val fødselsdato = LocalDate.now().minusMonths(6).plusWeeks(1)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `barn blir seks mnd om 1 uker pluss en dag, sjekk om fyller innen 1 uke, forvent ingen kall`() {
        val fødselsdato = LocalDate.now().minusMonths(6).plusWeeks(1).plusDays(1)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `7 av 14 barn blir 6 mnd innen 1 uke, sjekk fyller innen 1 uke, forvent 7 kall til beskrivelseBarnBlirSeksMnd`() {
        val fødselsdatoer = (0..14).asSequence().map { LocalDate.now().minusMonths(6).plusDays(it.toLong()) }.toList()
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns fødselsdatoer.map { Barn(generateFnr(it)) }
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
        verify(exactly = 7) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
    }

    @Test
    fun `7 av 14 barn blir 1 år innen 1 uke, sjekk fyller innen 1 uke, forvent 7 kall til beskrivelseBarnFyllerEttÅr`() {
        val fødselsdatoer = (0..14).asSequence().map { LocalDate.now().minusYears(1).plusDays(it.toLong()) }.toList()
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns fødselsdatoer.map { Barn(generateFnr(it)) }
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 7) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
    }

    @Test
    fun `barn blir 1 år om 4 dager, sjekk om fyller innen 1 uke, forvent kall til beskrivelseBarnFyllerEttÅr`() {
        val fødselsdato = LocalDate.now().minusYears(1).plusDays(3)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `barn blir 1 år i dag, sjekk om fyller innen 1 uke, forvent ingen kall`() {
        val fødselsdato = LocalDate.now().minusYears(1)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    @Test
    fun `barn blir 1 år om 7 dager, sjekk om fyller innen 1 uke, forvent kall til beskrivelseBarnFyllerEttÅr`() {
        val fødselsdato = LocalDate.now().minusYears(1).plusDays(7)
        every { iverksettingRepository.hentAlleBehandlinger() } returns listOf(iverksett)
        every { iverksett.søker.barn } returns listOf(opprettBarn(generateFnr(fødselsdato)))
        opprettOppgaveForBarnService.opprettOppgaverForAlleBarnSomFyller(innenAntallUker = 1)
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd() }
        verify { OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr() }
    }

    private fun generateFnr(localDate: LocalDate): String {
        return FnrGenerator.generer(localDate.year, localDate.month.value, localDate.dayOfMonth, false)
    }

    private fun opprettBarn(generertFnr: String): Barn {
        return Barn(generertFnr, null)
    }


}