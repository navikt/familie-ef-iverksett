package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.ef.iverksett.brev.frittstående.OppdaterKarakterinnhentingOppgaveTask.Companion.utledBeskrivelseForKarakterinnhentingOppgave
import no.nav.familie.ef.iverksett.brev.frittstående.OppdaterKarakterinnhentingOppgaveTask.Companion.utledFristForKarakterinnhentingOppgave
import no.nav.familie.ef.iverksett.brev.frittstående.OppdaterKarakterinnhentingOppgaveTask.Companion.utledPrioritetForKarakterinnhentingOppgave
import no.nav.familie.ef.iverksett.felles.util.dagensDatoNorskFormat
import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.Year
import java.util.UUID

internal class OppdaterKarakterinnhentingOppgaveTaskTest {

    private val karakterutskriftBrevRepository = mockk<KarakterutskriftBrevRepository>()
    private val oppgaveService = mockk<OppgaveService>()

    private val fristHovedperiode = "2023-05-17"
    private val fristUtvidet = "2023-05-18"

    private val oppdaterOppgaveTask = OppdaterKarakterinnhentingOppgaveTask(karakterutskriftBrevRepository, oppgaveService)
    private val oppgaveSlot = slot<Oppgave>()

    @BeforeEach
    fun setUp() {
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns brev()
        every { oppgaveService.oppdaterOppgave(capture(oppgaveSlot)) } returns 1L
    }

    @Test
    fun `skal oppdatere oppgave med tidligere beskrivelse og frist hovedperiode`() {
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "Oppgave opprettet.", frist = fristHovedperiode)
        oppdaterOppgaveTask.doTask(Task(OppdaterKarakterinnhentingOppgaveTask.TYPE, UUID.randomUUID().toString()))

        val oppdaterteVerdier = oppgaveSlot.captured

        verify(exactly = 1) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(oppdaterteVerdier.id).isEqualTo(5L)
        assertThat(oppdaterteVerdier.beskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
        assertThat(oppdaterteVerdier.beskrivelse).contains("Utført av familie-ef-sak ---")
        assertThat(oppdaterteVerdier.beskrivelse).contains(nyBeskrivelseEtTidligereInnslag)
        assertThat(oppdaterteVerdier.prioritet).isEqualTo(OppgavePrioritet.NORM)
        assertThat(oppdaterteVerdier.fristFerdigstillelse).isEqualTo("2023-08-05")
    }

    @Test
    fun `skal oppdatere oppgave uten tidligere beskrivelse og frist utvidet periode`() {
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns brev(FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_UTVIDET_PERIODE)
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = fristUtvidet)
        oppdaterOppgaveTask.doTask(Task(OppdaterKarakterinnhentingOppgaveTask.TYPE, UUID.randomUUID().toString()))

        val oppdaterteVerdier = oppgaveSlot.captured

        verify(exactly = 1) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(oppdaterteVerdier.id).isEqualTo(5L)
        assertThat(oppdaterteVerdier.beskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
        assertThat(oppdaterteVerdier.beskrivelse).contains("Utført av familie-ef-sak ---")
        assertThat(oppdaterteVerdier.beskrivelse).contains("Brev om innhenting av karakterutskrift er sendt til bruker.")
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
                    OppdaterKarakterinnhentingOppgaveTask.TYPE,
                    UUID.randomUUID().toString(),
                ),
            )
        }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Kan ikke oppdatere prioritet på oppgave med id=5")
    }

    @Test
    fun `skal kaste feil dersom oppgavefristen er endret underveis i flyten for innhenting av karakterutskrift - utvidet periode`() {
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns brev(FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_UTVIDET_PERIODE)
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = fristHovedperiode)

        val feil = assertThrows<IllegalStateException> {
            oppdaterOppgaveTask.doTask(
                Task(
                    OppdaterKarakterinnhentingOppgaveTask.TYPE,
                    UUID.randomUUID().toString(),
                ),
            )
        }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Oppgaven har blitt endret på underveis i flyten for innhenting av karakterutskrift.")
    }

    @Test
    fun `skal kaste feil dersom oppgavefristen er endret underveis i flyten for innhenting av karakterutskrift - hovedperiode`() {
        every { karakterutskriftBrevRepository.findByIdOrThrow(any()) } returns brev(FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE)
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = fristUtvidet)

        val feil = assertThrows<IllegalStateException> {
            oppdaterOppgaveTask.doTask(
                Task(
                    OppdaterKarakterinnhentingOppgaveTask.TYPE,
                    UUID.randomUUID().toString(),
                ),
            )
        }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Oppgaven har blitt endret på underveis i flyten for innhenting av karakterutskrift.")
    }

    @Nested
    inner class Prioritet {

        @Test
        internal fun `skal utlede riktig prioritet basert på oppgavens tidligere frist`() {
            val prioritetNormal = utledPrioritetForKarakterinnhentingOppgave(fristHovedperiode, 1L)
            val prioritetLav = utledPrioritetForKarakterinnhentingOppgave(fristUtvidet, 1L)

            assertThat(prioritetNormal).isEqualTo(OppgavePrioritet.NORM)
            assertThat(prioritetLav).isEqualTo(OppgavePrioritet.LAV)
        }

        @Test
        internal fun `skal kaste feil dersom ugyldig frist benyttes for å utlede prioritet`() {
            val feil = assertThrows<IllegalStateException> { utledPrioritetForKarakterinnhentingOppgave("2023-01-01", 1L) }

            assertThat(feil.message).contains("Kan ikke oppdatere prioritet på oppgave med id=")
        }
    }

    @Nested
    inner class Beskrivelse {

        @Test
        internal fun `skal utlede riktig beskrivelse basert på tidligere beskrivelse`() {
            val ingenTidligereBeskrivelse = utledBeskrivelseForKarakterinnhentingOppgave("")
            val tidligereBeskrivelse = utledBeskrivelseForKarakterinnhentingOppgave(tidligereOppgaveBeskrivelse)

            assertThat(ingenTidligereBeskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
            assertThat(ingenTidligereBeskrivelse).contains("Utført av familie-ef-sak ---")
            assertThat(ingenTidligereBeskrivelse).contains(nyttBeskrivelseInnslag)

            assertThat(tidligereBeskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
            assertThat(tidligereBeskrivelse).contains("Utført av familie-ef-sak ---")
            assertThat(tidligereBeskrivelse).contains(nyBeskrivelseToTidligereInnslag)
        }
    }

    @Nested
    inner class Frist {

        @Test
        internal fun `skal utlede riktig frist oppgavens tidligere frist`() {
            val fristFemteAugust = utledFristForKarakterinnhentingOppgave(fristHovedperiode, 1L)
            val fristSjetteAugust = utledFristForKarakterinnhentingOppgave(fristUtvidet, 1L)

            assertThat(fristFemteAugust).isEqualTo(LocalDate.parse("2023-08-05"))
            assertThat(fristSjetteAugust).isEqualTo(LocalDate.parse("2023-08-06"))
        }

        @Test
        internal fun `skal kaste feil dersom ugyldig frist benyttes for å utlede ny frist`() {
            val feil = assertThrows<IllegalStateException> { utledFristForKarakterinnhentingOppgave("2023-01-01", 1L) }

            assertThat(feil.message).contains("Kan ikke oppdatere frist på oppgave med id=")
        }
    }

    private val nyttBeskrivelseInnslag = "Brev om innhenting av karakterutskrift er sendt til bruker."

    private val tidligereOppgaveBeskrivelse = "Oppgave opprettet.\n\nOppgave lagt i mappe 64."

    private val nyBeskrivelseToTidligereInnslag = "Brev om innhenting av karakterutskrift er sendt til bruker.\n\n" +
        "Oppgave opprettet.\n\n" + "Oppgave lagt i mappe 64."

    private val nyBeskrivelseEtTidligereInnslag = "Brev om innhenting av karakterutskrift er sendt til bruker.\n\nOppgave opprettet."

    private fun oppgave(beskrivelse: String, frist: String) =
        Oppgave(id = 5L, beskrivelse = beskrivelse, fristFerdigstillelse = frist)

    private fun brev(brevType: FrittståendeBrevType = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE) =
        KarakterutskriftBrev(
            id = UUID.randomUUID(),
            brevtype = brevType,
            eksternFagsakId = 6L,
            personIdent = "",
            oppgaveId = 5L,
            journalførendeEnhet = "",
            fil = ByteArray(1),
            gjeldendeÅr = Year.now(),
            stønadType = StønadType.OVERGANGSSTØNAD,
        )
}
