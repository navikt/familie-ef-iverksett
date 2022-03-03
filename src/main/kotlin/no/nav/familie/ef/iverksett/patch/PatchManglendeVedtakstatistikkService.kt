package no.nav.familie.ef.iverksett.patch

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@Unprotected
@RequestMapping("api/patch-vedtakstatistikk")
class PatchManglendeVedtakstatistikkController(private val patchManglendeVedtakstatistikkService: PatchManglendeVedtakstatistikkService) {

    @GetMapping
    fun patch(@RequestParam lagTask: Boolean = false) {
        patchManglendeVedtakstatistikkService.patchVedtakstatistikk(lagTask)
    }
}

@Service
class PatchManglendeVedtakstatistikkService(
        private val iverksettingRepository: IverksettingRepository,
        private val taskRepository: TaskRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun patchVedtakstatistikk(lagTask: Boolean) {

        val migrerteBehandlingIds = iverksettingRepository.hentAlleMigrerte()
        logger.info("Antall migrerte behandlinger ${migrerteBehandlingIds.size} - oppdateres: $lagTask")
        for (behandlingId in migrerteBehandlingIds) {
            if (lagTask) {
                taskRepository.save(Task(
                        type = VedtakstatistikkTask.TYPE,
                        payload = behandlingId.toString(),
                ))
            }
        }
        if (lagTask) {
            logger.info("Alle ${migrerteBehandlingIds.size} tasks opprettet")
        }
    }
}