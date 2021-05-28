package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.økonomi.grensesnitt.GrensesnittavstemmingPayload
import no.nav.familie.ef.iverksett.økonomi.grensesnitt.GrensesnittavstemmingTask
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class GrensesnittavstemmingTaskTest {

    val oppdragClient = mockk<OppdragClient>()
    val taskRepository = mockk<TaskRepository>()
    val grensesnittavstemmingTask = GrensesnittavstemmingTask(oppdragClient, taskRepository)


    @Test
    fun `doTask skal kalle oppdragClient med fradato fra payload og dato for triggerTid som parametere`() {
        val grensesnittavstemmingRequestSlot = slot<GrensesnittavstemmingRequest>()

        every { oppdragClient.grensesnittavstemming(any()) }.returns("ok")

        grensesnittavstemmingTask.doTask(Task(type = GrensesnittavstemmingTask.TYPE,
                                              payload = payload,
                                              triggerTid = LocalDateTime.of(2018, 4, 19, 8, 0)))
        verify(exactly = 1) { oppdragClient.grensesnittavstemming(capture(grensesnittavstemmingRequestSlot)) }
        val capturedGrensesnittRequest = grensesnittavstemmingRequestSlot.captured
        assertThat(capturedGrensesnittRequest.fra).isEqualTo(LocalDate.of(2018, 4, 18).atStartOfDay())
        assertThat(capturedGrensesnittRequest.til).isEqualTo(LocalDate.of(2018, 4, 19).atStartOfDay())
        assertThat(capturedGrensesnittRequest.fagsystem).isEqualTo(StønadType.OVERGANGSSTØNAD.tilKlassifisering())
    }

    @Test
    fun `onCompletion skal opprette ny grensesnittavstemmingTask med dato for forrige triggerTid som payload`() {
        val triggeTid = LocalDateTime.of(2018, 4, 19, 8, 0)
        val slot = slot<Task>()
        every { taskRepository.save(capture(slot)) } returns mockk()

        grensesnittavstemmingTask.onCompletion(Task(type = GrensesnittavstemmingTask.TYPE,
                                                    payload = payload,
                                                    triggerTid = triggeTid))
        val forventetPayload = objectMapper.writeValueAsString(GrensesnittavstemmingPayload(fraDato = LocalDate.of(2018, 4, 19),
                                                                                            stønadstype = StønadType.OVERGANGSSTØNAD))
        assertThat(slot.captured.payload).isEqualTo(forventetPayload)
    }

    companion object {

        val payload = objectMapper.writeValueAsString(GrensesnittavstemmingPayload(fraDato = LocalDate.of(2018, 4, 18),
                                                                                   stønadstype = StønadType.OVERGANGSSTØNAD))
    }
}