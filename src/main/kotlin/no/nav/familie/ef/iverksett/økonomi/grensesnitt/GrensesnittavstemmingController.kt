package no.nav.familie.ef.iverksett.økonomi.grensesnitt

import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping(path = ["/api/grensesnittavstemming"])
@Unprotected
class GrensesnittavstemmingController(val taskRepository: TaskRepository) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    fun settIGangGrensesnittavstemming(): ResponseEntity<Ressurs<String>> {
        val iDag = LocalDate.now()

        logger.info("Lager task for grensesnittavstemming")
        val grensesnittavstemmingTask =
                GrensesnittavstemmingDto(StønadType.OVERGANGSSTØNAD, iDag.minusDays(1), iDag.atStartOfDay()).tilTask()
        taskRepository.save(grensesnittavstemmingTask)

        return ResponseEntity.ok(Ressurs.success("Laget task for grensesnittavstemming"))
    }
}