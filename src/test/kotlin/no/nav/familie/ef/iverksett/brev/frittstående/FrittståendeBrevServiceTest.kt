package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.ef.iverksett.brev.domain.tilDto
import no.nav.familie.ef.iverksett.brev.frittstående.KarakterInnhentingBrevUtil.opprettBrev
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FrittståendeBrevServiceTest {

    private val frittståendeBrevRepository = mockk<FrittståendeBrevRepository>()
    private val karakterutskriftBrevRepository = mockk<KarakterutskriftBrevRepository>()
    private val taskService = mockk<TaskService>()
    private val journalpostClient = mockk<JournalpostClient>()

    private val frittståendeBrevService =
        FrittståendeBrevService(frittståendeBrevRepository, karakterutskriftBrevRepository, taskService, journalpostClient)

    private val hovedtype = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE

    private val brevSlot = slot<KarakterutskriftBrev>()
    private val taskSlot = slot<Task>()

    @Nested inner class KarakterInnhentingBrev {
        @Test
        internal fun `skal lagre ned brev og journalføringstask`() {
            val brev = opprettBrev(hovedtype)
            val brevDto = brev.tilDto()

            every { karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns false
            every { karakterutskriftBrevRepository.existsByEksternFagsakIdAndGjeldendeÅrAndBrevtype(any(), any(), any()) } returns false
            every { karakterutskriftBrevRepository.insert(capture(brevSlot)) } answers { firstArg<KarakterutskriftBrev>().copy(id = brev.id) }
            every { taskService.save(capture(taskSlot)) } answers { firstArg() }

            frittståendeBrevService.opprettTask(brevDto)

            verify(exactly = 1) { karakterutskriftBrevRepository.insert(any()) }
            verify(exactly = 1) { taskService.save(any()) }
            assertThat(brevSlot.captured.tilDto()).isEqualTo(brevDto)
            assertThat(taskSlot.captured.type).isEqualTo(JournalførKarakterutskriftBrevTask.TYPE)
            assertThat(taskSlot.captured.payload).isEqualTo(brev.id.toString())
        }

        @Test
        internal fun `skal ikke lagre brev men kaste feil dersom brevtypen ikke er gyldig`() {
            val brev = opprettBrev(FrittståendeBrevType.BREV_OM_SVARTID_KLAGE)
            val brevDto = brev.tilDto()

            every { karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns false

            val feil = assertThrows<ApiFeil> { frittståendeBrevService.opprettTask(brevDto) }

            verify(exactly = 0) { karakterutskriftBrevRepository.insert(any()) }
            verify(exactly = 0) { taskService.save(any()) }
            assertThat(feil.feil).isEqualTo("Skal ikke opprette automatiske innhentingsbrev for frittstående brev av type ${brevDto.brevtype}")
        }

        @Test
        internal fun `skal ikke lagre brev men kaste feil dersom brevet allerede er lagret for oppgaven`() {
            val brev = opprettBrev(hovedtype)
            val brevDto = brev.tilDto()

            every { karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns true

            val feil = assertThrows<ApiFeil> { frittståendeBrevService.opprettTask(brevDto) }

            verify(exactly = 0) { karakterutskriftBrevRepository.insert(any()) }
            verify(exactly = 0) { taskService.save(any()) }
            assertThat(feil.feil).isEqualTo("Skal ikke kunne opprette flere innhentingsbrev for fagsak med eksternId=${brevDto.eksternFagsakId}")
        }

        @Test
        internal fun `skal ikke lagre brev men kaste feil dersom brevet allerede er lagret for brukeren`() {
            val brev = opprettBrev(hovedtype)
            val brevDto = brev.tilDto()

            every { karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(any(), any(), any()) } returns false
            every { karakterutskriftBrevRepository.existsByEksternFagsakIdAndGjeldendeÅrAndBrevtype(any(), any(), any()) } returns true

            val feil = assertThrows<ApiFeil> { frittståendeBrevService.opprettTask(brevDto) }

            verify(exactly = 0) { karakterutskriftBrevRepository.insert(any()) }
            verify(exactly = 0) { taskService.save(any()) }
            assertThat(feil.feil).isEqualTo("Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=${brevDto.eksternFagsakId}")
        }
    }
}
