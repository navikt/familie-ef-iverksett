package no.nav.familie.ef.iverksett.infrastruktur.service

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/jsonUpdate"])
@Unprotected
class JsonSchemaService(
    private val taskService: TaskService,
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettResultatRepository: IverksettResultatRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    @GetMapping
    fun update(): String {
        val iverksettingIder = iverksettingRepository.finnAlleIder()

        log.info("Starter oppretting av tasker for oppdatering av json på iverksett. Antall ${iverksettingIder.size}")
        iverksettingIder.forEach {
            taskService.save(Task(JsonUpdatePeriodeIverksettTask.TYPE, it.toString()))
        }
        log.info("oppretting av ${iverksettingIder.size} tasker fullført")

        val iverksettingresultatIder = iverksettResultatRepository.finnAlleIder()
        log.info(
            "Starter oppretting av tasker for oppdatering av json på iverksettResultat. " +
                "Antall ${iverksettingresultatIder.size}"
        )
        iverksettingresultatIder.forEach {
            taskService.save(Task(JsonUpdatePeriodeIverksettResultatTask.TYPE, it.toString()))
        }

        log.info("oppretting av ${iverksettingresultatIder.size} tasker fullført")

        return "Opprettet ${iverksettingIder.size} tasker for oppdatering av iverksetting " +
            "og ${iverksettingresultatIder.size} tasker for oppdatering av iverksettResultat."
    }
}

@Service
@TaskStepBeskrivelse(
    taskStepType = JsonUpdatePeriodeIverksettTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Oppdaterer Json-data."
)
class JsonUpdatePeriodeIverksettTask(
    private val iverksettingRepository: IverksettingRepository
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val iverksett = iverksettingRepository.findByIdOrThrow(UUID.fromString(task.payload))
        iverksettingRepository.update(iverksett)
    }

    companion object {
        const val TYPE = "JsonUpdatePeriodeIverksett"
    }
}

@Service
@TaskStepBeskrivelse(
    taskStepType = JsonUpdatePeriodeIverksettResultatTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Oppdaterer Json-data."
)
class JsonUpdatePeriodeIverksettResultatTask(private val iverksettResultatRepository: IverksettResultatRepository) :
    AsyncTaskStep {
    override fun doTask(task: Task) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(UUID.fromString(task.payload))
        iverksettResultatRepository.update(iverksettResultat)
    }

    companion object {
        const val TYPE = "JsonUpdatePeriodeIverksettResultat"
    }
}
