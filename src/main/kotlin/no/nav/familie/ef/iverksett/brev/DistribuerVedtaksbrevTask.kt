package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = DistribuerVedtaksbrevTask.TYPE,
                     maxAntallFeil = 50,
                     settTilManuellOppfølgning = true,
                     triggerTidVedFeilISekunder = 15 * 60L,
                     beskrivelse = "Distribuerer vedtaksbrev.")
class DistribuerVedtaksbrevTask(private val journalpostClient: JournalpostClient,
                                private val tilstandRepository: TilstandRepository) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val journalpostId = tilstandRepository.hentJournalpostResultat(behandlingId)?.journalpostId
        val bestillingId = journalpostId?.let { journalpostClient.distribuerBrev(it) }
        tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId = behandlingId,
                                                                 DistribuerVedtaksbrevResultat(bestillingId = bestillingId)
        )
        logger.info("Distribuer vedtaksbrev journalpost=[${journalpostId}] for behandling=[${behandlingId}] med bestillingId=[$bestillingId]")
    }

    companion object {

        const val TYPE = "distribuerVedtaksbrev"
    }
}