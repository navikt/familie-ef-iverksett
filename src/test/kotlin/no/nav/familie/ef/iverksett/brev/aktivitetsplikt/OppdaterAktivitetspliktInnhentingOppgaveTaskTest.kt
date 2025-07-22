package no.nav.familie.ef.iverksett.brev.aktivitetsplikt

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.OppdaterAktivitetspliktInnhentingOppgaveTask.Companion.utledBeskrivelseForAktivitetspliktOppgave
import no.nav.familie.ef.iverksett.brev.domain.AktivitetspliktBrev
import no.nav.familie.ef.iverksett.felles.util.dagensDatoNorskFormat
import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
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
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.util.UUID

internal class OppdaterAktivitetspliktInnhentingOppgaveTaskTest {
    private val aktivitetspliktBrevRepository = mockk<AktivitetspliktBrevRepository>()
    private val oppgaveService = mockk<OppgaveService>()

    private val initiellFristPåOppgave = "2025-05-17"
    private val oppdatertFrist = "2025-07-27"

    private val oppdaterOppgaveTask = OppdaterAktivitetspliktInnhentingOppgaveTask(aktivitetspliktBrevRepository, oppgaveService)
    private val oppgaveSlot = slot<Oppgave>()

    @BeforeEach
    fun setUp() {
        every { aktivitetspliktBrevRepository.findByIdOrThrow(any()) } returns brev()
        every { oppgaveService.oppdaterOppgave(capture(oppgaveSlot)) } returns 1L
    }

    @Test
    fun `skal oppdatere oppgave med tidligere beskrivelse og frist hovedperiode`() {
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "Oppgave opprettet.", frist = initiellFristPåOppgave)
        oppdaterOppgaveTask.doTask(Task(OppdaterAktivitetspliktInnhentingOppgaveTask.TYPE, UUID.randomUUID().toString()))

        val oppdaterteVerdier = oppgaveSlot.captured

        verify(exactly = 1) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(oppdaterteVerdier.id).isEqualTo(5L)
        assertThat(oppdaterteVerdier.beskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
        assertThat(oppdaterteVerdier.beskrivelse).contains("Utført av familie-ef-sak ---")
        assertThat(oppdaterteVerdier.beskrivelse).contains(nyBeskrivelseEtTidligereInnslag)
        assertThat(oppdaterteVerdier.prioritet).isEqualTo(OppgavePrioritet.NORM)
        assertThat(oppdaterteVerdier.fristFerdigstillelse).isEqualTo(oppdatertFrist)
    }

    @Test
    fun `skal oppdatere oppgave uten tidligere beskrivelse`() {
        every {
            aktivitetspliktBrevRepository.findByIdOrThrow(any())
        } returns brev()
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = initiellFristPåOppgave)
        oppdaterOppgaveTask.doTask(Task(OppdaterAktivitetspliktInnhentingOppgaveTask.TYPE, UUID.randomUUID().toString()))

        val oppdaterteVerdier = oppgaveSlot.captured

        verify(exactly = 1) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(oppdaterteVerdier.id).isEqualTo(5L)
        assertThat(oppdaterteVerdier.beskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
        assertThat(oppdaterteVerdier.beskrivelse).contains("Utført av familie-ef-sak ---")
        assertThat(oppdaterteVerdier.beskrivelse).contains("Brev om innhenting av dokumentasjon på videre aktivitet er sendt til bruker.")
        assertThat(oppdaterteVerdier.prioritet).isEqualTo(OppgavePrioritet.NORM)
        assertThat(oppdaterteVerdier.fristFerdigstillelse).isEqualTo(oppdatertFrist)
    }

    @Test
    fun `skal kaste feil dersom den opprinnelige oppgavefristen ikke er riktig`() {
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = "2023-05-10")

        val feil =
            assertThrows<IllegalStateException> {
                oppdaterOppgaveTask.doTask(
                    Task(
                        OppdaterAktivitetspliktInnhentingOppgaveTask.TYPE,
                        UUID.randomUUID().toString(),
                    ),
                )
            }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Kan ikke oppdatere verdier på oppgave med id=5")
    }

    @Test
    fun `skal kaste feil dersom oppgavefristen er endret underveis i flyten for innhenting av aktivitetsplikt`() {
        every {
            aktivitetspliktBrevRepository.findByIdOrThrow(any())
        } returns brev()
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = "2023-01-01")

        val feil =
            assertThrows<IllegalStateException> {
                oppdaterOppgaveTask.doTask(
                    Task(
                        OppdaterAktivitetspliktInnhentingOppgaveTask.TYPE,
                        UUID.randomUUID().toString(),
                    ),
                )
            }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Oppgaven har blitt endret på underveis i flyten for innhenting av aktivitetsplikt.")
    }

    @Test
    fun `skal kaste feil dersom oppgavefristen er endret underveis i flyten`() {
        every {
            aktivitetspliktBrevRepository.findByIdOrThrow(any())
        } returns brev()
        every { oppgaveService.hentOppgave(any()) } returns
            oppgave(beskrivelse = "", frist = "2024-01-01")

        val feil =
            assertThrows<IllegalStateException> {
                oppdaterOppgaveTask.doTask(
                    Task(
                        OppdaterAktivitetspliktInnhentingOppgaveTask.TYPE,
                        UUID.randomUUID().toString(),
                    ),
                )
            }

        verify(exactly = 0) { oppgaveService.oppdaterOppgave(any()) }
        assertThat(feil.message).contains("Oppgaven har blitt endret på underveis i flyten for innhenting av aktivitetsplikt.")
    }

    @Test
    fun `skal huske å oppdatere gjeldende frist innen juni neste år`() {
        if (YearMonth.now().month >= Month.JUNE) {
            assertThat(LocalDate.parse(OppdaterAktivitetspliktInnhentingOppgaveTask.FRIST_OPPRINNELIG_OPPGAVE).year).isEqualTo(YearMonth.now().year)
            assertThat(LocalDate.parse(OppdaterAktivitetspliktInnhentingOppgaveTask.FRIST_OPPFØLGINGSOPPGAVE).year).isEqualTo(YearMonth.now().year)
        }
    }

    @Nested
    inner class Beskrivelse {
        @Test
        internal fun `skal utlede riktig beskrivelse basert på tidligere beskrivelse`() {
            val ingenTidligereBeskrivelse = utledBeskrivelseForAktivitetspliktOppgave("")
            val tidligereBeskrivelse = utledBeskrivelseForAktivitetspliktOppgave(tidligereOppgaveBeskrivelse)

            assertThat(ingenTidligereBeskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
            assertThat(ingenTidligereBeskrivelse).contains("Utført av familie-ef-sak ---")
            assertThat(ingenTidligereBeskrivelse).contains(nyttBeskrivelseInnslag)

            assertThat(tidligereBeskrivelse).startsWith("--- ${dagensDatoNorskFormat()} ")
            assertThat(tidligereBeskrivelse).contains("Utført av familie-ef-sak ---")
            assertThat(tidligereBeskrivelse).contains(nyBeskrivelseToTidligereInnslag)
        }
    }

    private val nyttBeskrivelseInnslag = "Brev om innhenting av dokumentasjon på videre aktivitet er sendt til bruker."

    private val tidligereOppgaveBeskrivelse = "Oppgave opprettet.\n\nOppgave lagt i mappe 64."

    private val nyBeskrivelseToTidligereInnslag =
        "Brev om innhenting av dokumentasjon på videre aktivitet er sendt til bruker.\n\n" +
            "Oppgave opprettet.\n\n" +
            "Oppgave lagt i mappe 64."

    private val nyBeskrivelseEtTidligereInnslag = "Brev om innhenting av dokumentasjon på videre aktivitet er sendt til bruker.\n\nOppgave opprettet."

    private fun oppgave(
        beskrivelse: String,
        frist: String,
    ) = Oppgave(id = 5L, beskrivelse = beskrivelse, fristFerdigstillelse = frist)

    private fun brev() =
        AktivitetspliktBrev(
            id = UUID.randomUUID(),
            eksternFagsakId = 6L,
            personIdent = "",
            oppgaveId = 5L,
            journalførendeEnhet = "",
            fil = ByteArray(1),
            gjeldendeÅr = Year.now(),
            stønadType = StønadType.OVERGANGSSTØNAD,
        )
}
