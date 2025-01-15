package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.AktivitetspliktBrevRepository
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.AktivitetspliktInnhentingBrevUtil.opprettBrev
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.JournalførAktivitetspliktutskriftBrevTask
import no.nav.familie.ef.iverksett.brev.domain.AktivitetspliktBrev
import no.nav.familie.ef.iverksett.brev.domain.tilDto
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FrittståendeBrevServiceTest {
    private val frittståendeBrevRepository = mockk<FrittståendeBrevRepository>()
    private val aktivitetspliktBrevRepository = mockk<AktivitetspliktBrevRepository>()
    private val taskService = mockk<TaskService>()

    private val frittståendeBrevService =
        FrittståendeBrevService(frittståendeBrevRepository, aktivitetspliktBrevRepository, taskService)

    private val brevSlot = slot<AktivitetspliktBrev>()
    private val taskSlot = slot<Task>()

    @Nested inner class KarakterInnhentingBrev {
        @Test
        internal fun `skal lagre ned brev og journalføringstask`() {
            val brev = opprettBrev()
            val brevDto = brev.tilDto()

            every { aktivitetspliktBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns false
            every { aktivitetspliktBrevRepository.existsByEksternFagsakIdAndGjeldendeÅr(any(), any()) } returns false
            every {
                aktivitetspliktBrevRepository.insert(
                    capture(brevSlot),
                )
            } answers { firstArg<AktivitetspliktBrev>().copy(id = brev.id) }
            every { taskService.save(capture(taskSlot)) } answers { firstArg() }

            frittståendeBrevService.opprettTaskForInnhentingAvAktivitetsplikt(brevDto)

            verify(exactly = 1) { aktivitetspliktBrevRepository.insert(any()) }
            verify(exactly = 1) { taskService.save(any()) }
            assertThat(brevSlot.captured.tilDto()).isEqualTo(brevDto)
            assertThat(taskSlot.captured.type).isEqualTo(JournalførAktivitetspliktutskriftBrevTask.TYPE)
            assertThat(taskSlot.captured.payload).isEqualTo(brev.id.toString())
        }

        @Test
        internal fun `skal ikke lagre brev men kaste feil dersom brevet allerede er lagret for oppgaven`() {
            val brev = opprettBrev()
            val brevDto = brev.tilDto()

            every { aktivitetspliktBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns true

            val feil = assertThrows<ApiFeil> { frittståendeBrevService.opprettTaskForInnhentingAvAktivitetsplikt(brevDto) }

            verify(exactly = 0) { aktivitetspliktBrevRepository.insert(any()) }
            verify(exactly = 0) { taskService.save(any()) }
            assertThat(
                feil.feil,
            ).isEqualTo("Skal ikke kunne opprette flere innhentingsbrev for fagsak med eksternId=${brevDto.eksternFagsakId}")
        }

        @Test
        internal fun `skal ikke lagre brev men kaste feil dersom brevet allerede er lagret for brukeren`() {
            val brev = opprettBrev()
            val brevDto = brev.tilDto()

            every { aktivitetspliktBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns false
            every { aktivitetspliktBrevRepository.existsByEksternFagsakIdAndGjeldendeÅr(any(), any()) } returns true

            val feil = assertThrows<ApiFeil> { frittståendeBrevService.opprettTaskForInnhentingAvAktivitetsplikt(brevDto) }

            verify(exactly = 0) { aktivitetspliktBrevRepository.insert(any()) }
            verify(exactly = 0) { taskService.save(any()) }
            assertThat(
                feil.feil,
            ).isEqualTo("Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=${brevDto.eksternFagsakId}")
        }
    }
}
