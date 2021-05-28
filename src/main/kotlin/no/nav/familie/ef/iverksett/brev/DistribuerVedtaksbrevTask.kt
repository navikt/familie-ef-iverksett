package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksettingstatus.status.tilstand.TilstandDbUtil
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
                                val tilstandDbUtil: TilstandDbUtil,
                                val henteTilstandService: TilstandDbUtil
) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val journalpostId = henteTilstandService.hentJournalpostResultat(behandlingId)?.journalpostId
        val bestillingId = journalpostId?.let { journalpostClient.distribuerBrev(it) }
        tilstandDbUtil.lagreDistribuerVedtaksbrevResultat(behandlingId = behandlingId,
                                                          DistribuerVedtaksbrevResultat(bestillingId = bestillingId)
        )
        logger.info("Distribuer vedtaksbrev journalpost=[${journalpostId}] for behandling=[${behandlingId}] med bestillingId=[$bestillingId]")
    }

    companion object {

        const val TYPE = "distribuerVedtaksbrev"
    }
}