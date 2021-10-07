package no.nav.familie.ef.iverksett.økonomi.grensesnitt

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

data class GrensesnittavstemmingPayload(val fraDato: LocalDate, val stønadstype: StønadType)

@Service
@TaskStepBeskrivelse(taskStepType = GrensesnittavstemmingTask.TYPE, beskrivelse = "Utfører grensesnittavstemming mot økonomi.")
class GrensesnittavstemmingTask(private val oppdragClient: OppdragClient,
                                private val taskRepository: TaskRepository) : AsyncTaskStep {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun doTask(task: Task) {
        with(objectMapper.readValue<GrensesnittavstemmingPayload>(task.payload)) {
            val fraTidspunkt = fraDato.atStartOfDay()
            val tilTidspunkt = task.triggerTid.toLocalDate().atStartOfDay()

            logger.info("Gjør ${task.id} $stønadstype avstemming mot oppdrag fra $fraTidspunkt til $tilTidspunkt")
            val grensesnittavstemmingRequest = GrensesnittavstemmingRequest(fagsystem = stønadstype.tilKlassifisering(),
                                                                            fra = fraTidspunkt,
                                                                            til = tilTidspunkt)
            oppdragClient.grensesnittavstemming(grensesnittavstemmingRequest)
        }
    }

    override fun onCompletion(task: Task) {
        val payload = objectMapper.readValue<GrensesnittavstemmingPayload>(task.payload)
        val nesteFradato = task.triggerTid.toLocalDate()
        opprettGrensesnittavstemmingTask(GrensesnittavstemmingDto(stønadstype = payload.stønadstype, fraDato = nesteFradato))
    }

    fun opprettGrensesnittavstemmingTask(grensesnittavstemmingDto: GrensesnittavstemmingDto) =
            grensesnittavstemmingDto.tilTask()
                    .let { taskRepository.save(it) }

    companion object {

        const val TYPE = "utførGrensesnittavstemming"
    }
}
