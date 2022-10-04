package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Loggtype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.RekjørSenereException
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = DistribuerVedtaksbrevTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Distribuerer vedtaksbrev."
)
class DistribuerVedtaksbrevTask(
    private val journalpostClient: JournalpostClient,
    private val iverksettResultatService: IverksettResultatService
) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private sealed class Resultat
    private object OK : Resultat()
    private data class Dødsbo(val melding: String) : Resultat()

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)

        val resultat: Resultat = distribuerVedtaksbrev(behandlingId)

        if (resultat is Dødsbo) {
            håndterDødsbo(task, resultat)
        }
    }

    private fun distribuerVedtaksbrev(behandlingId: UUID): Resultat {
        val journalpostResultat = hentJournalpostResultat(behandlingId)
        val distribuerteJournalposter =
            iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId)?.keys ?: emptySet()

        var resultat: Dødsbo? = null
        journalpostResultat.filter { (_, journalpostResultat) ->
            journalpostResultat.journalpostId !in distribuerteJournalposter
        }.forEach { (personIdent, journalpostResultat) ->
            try {
                distribuerBrevOgOppdaterVedtaksbrevResultat(journalpostResultat, behandlingId)
            } catch (e: RessursException) {
                val cause = e.cause
                if (cause is HttpClientErrorException.Gone) {
                    resultat = Dødsbo("Dødsbo personIdent=$personIdent ${cause.responseBodyAsString}")
                } else {
                    throw e
                }
            }
        }
        return resultat ?: OK
    }

    private fun distribuerBrevOgOppdaterVedtaksbrevResultat(
        journalpostResultat: JournalpostResultat,
        behandlingId: UUID
    ) {
        val bestillingId = journalpostClient.distribuerBrev(journalpostResultat.journalpostId, Distribusjonstype.VEDTAK)
        loggBrevDistribuert(journalpostResultat.journalpostId, behandlingId, bestillingId)
        iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(
            behandlingId,
            journalpostResultat.journalpostId,
            DistribuerBrevResultat(bestillingId)
        )
    }

    private fun håndterDødsbo(task: Task, dødsbo: Dødsbo) {
        val antallRekjørSenerePgaDødsbo =
            task.logg.count { it.type == Loggtype.KLAR_TIL_PLUKK && it.melding?.startsWith("Dødsbo") == true }
        if (antallRekjørSenerePgaDødsbo < 7) {
            logger.warn("Mottaker for vedtaksbrev behandling=${task.payload} har dødsbo, prøver å sende brev på nytt om 7 dager")
            throw RekjørSenereException(dødsbo.melding, LocalDateTime.now().plusDays(7))
        } else {
            throw TaskExceptionUtenStackTrace("Er dødsbo og har feilet flere ganger: ${dødsbo.melding}")
        }
    }

    private fun hentJournalpostResultat(behandlingId: UUID): Map<String, JournalpostResultat> {
        val journalpostResultat = iverksettResultatService.hentJournalpostResultat(behandlingId)
        if (journalpostResultat.isNullOrEmpty()) {
            error("Fant ingen journalpost for behandling=[$behandlingId]")
        }
        return journalpostResultat
    }

    private fun loggBrevDistribuert(journalpostId: String, behandlingId: UUID, bestillingId: String) {
        logger.info(
            "Distribuer vedtaksbrev journalpost=[$journalpostId] " +
                "for behandling=[$behandlingId] med bestillingId=[$bestillingId]"
        )
    }

    companion object {

        const val TYPE = "distribuerVedtaksbrev"
    }
}
