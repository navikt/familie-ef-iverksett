package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
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
                                private val tilstandRepository: TilstandRepository,
                                private val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)

        validerJournalpostresultat(behandlingId)

        val journalpostResultat = tilstandRepository.hentJournalpostResultat(behandlingId)

        val distribuerteJournalposter = tilstandRepository.hentdistribuerVedtaksbrevResultat(behandlingId)?.keys ?: emptySet()

        journalpostResultat?.filter { (_, journalpostResultat) ->
            journalpostResultat.journalpostId !in distribuerteJournalposter
        }?.forEach { (_, journalpostResultat) ->
            val bestillingId = journalpostClient.distribuerBrev(journalpostResultat.journalpostId)
            loggBrevDistribuert(journalpostResultat.journalpostId, behandlingId, bestillingId)
            tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId,
                                                                                    journalpostResultat.journalpostId,
                                                                                    DistribuerVedtaksbrevResultat(bestillingId))
        }

    }

    private fun validerJournalpostresultat(behandlingId: UUID) {
        if (tilstandRepository.hentJournalpostResultat(behandlingId).isNullOrEmpty()){
            error("Fant ingen journalpost for behandling=[$behandlingId]")
        }
    }

    private fun loggBrevDistribuert(journalpostId: String, behandlingId: UUID, bestillingId: String) {
        logger.info("Distribuer vedtaksbrev journalpost=[${journalpostId}] " +
                    "for behandling=[${behandlingId}] med bestillingId=[$bestillingId]")
    }

    companion object {

        const val TYPE = "distribuerVedtaksbrev"
    }
}