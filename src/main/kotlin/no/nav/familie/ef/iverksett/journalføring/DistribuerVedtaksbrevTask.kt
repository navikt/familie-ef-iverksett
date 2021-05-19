package no.nav.familie.ef.iverksett.journalføring

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = DistribuerVedtaksbrevTask.TYPE,
                     maxAntallFeil = 50,
                     settTilManuellOppfølgning = true,
                     triggerTidVedFeilISekunder = 15 * 60L,
                     beskrivelse = "Distribuerer vedtaksbrev.")
class DistribuerVedtaksbrevTask(val journalpostClient: JournalpostClient,
                                val lagreTilstandService: LagreTilstandService
) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val taskData = objectMapper.readValue<DistribuerVedtaksbrevTaskData>(task.payload)
        val bestillingId = journalpostClient.distribuerBrev(taskData.journalpostId)
        lagreTilstandService.lagreDistribuerVedtaksbrevResultat(behandlingId = taskData.behandlingId,
                                                                DistribuerVedtaksbrevResultat(bestillingId = bestillingId)
        )
        logger.info("Distribuer vedtaksbrev journalpost=[${taskData.journalpostId}] for behandling=[${taskData.behandlingId}] med bestillingId=[$bestillingId]")
    }

    companion object {

        fun opprettTask(behandlingId: UUID, journalpostId: String): Task =
                Task(type = TYPE,
                     payload = objectMapper.writeValueAsString(DistribuerVedtaksbrevTaskData(behandlingId = behandlingId,
                                                                                             journalpostId = journalpostId)))

        const val TYPE = "distribuerVedtaksbrev"
    }

    data class DistribuerVedtaksbrevTaskData(val behandlingId: UUID, val journalpostId: String)
}