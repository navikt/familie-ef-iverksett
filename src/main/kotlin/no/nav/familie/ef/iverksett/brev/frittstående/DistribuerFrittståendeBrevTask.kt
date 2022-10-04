package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
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
    taskStepType = DistribuerFrittståendeBrevTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Distribuerer frittstående brev."
)
class DistribuerFrittståendeBrevTask(
    private val frittståendeBrevRepository: FrittståendeBrevRepository
) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private sealed class Resultat
    private object OK : Resultat()
    private data class Dødsbo(val melding: String) : Resultat()

    override fun doTask(task: Task) {
        val frittståendeBrevId = UUID.fromString(task.payload)

        val resultat: Resultat = distribuerFrittståendeBrev(frittståendeBrevId)

        if (resultat is Dødsbo) {
            håndterDødsbo(task, resultat)
        }
    }

    private fun distribuerFrittståendeBrev(frittståendeBrevId: UUID): Resultat {
        val journalpostResultat = hentFrittståendeBrev(frittståendeBrevId)
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
            DistribuerVedtaksbrevResultat(bestillingId)
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

    private fun hentFrittståendeBrev(frittståendeBrevId: UUID): Map<String, JournalpostResultat> {
        val frittståendeBrev = frittståendeBrevRepository.findByIdOrThrow(frittståendeBrevId)
        if (frittståendeBrev.journalposter.isEmpty()) {
            error("Fant ingen journalpost for frittståendeBrev=$frittståendeBrevId")
        }
        return frittståendeBrev.journalposter
    }

    private fun loggBrevDistribuert(journalpostId: String, behandlingId: UUID, bestillingId: String) {
        logger.info(
            "Distribuer vedtaksbrev journalpost=[$journalpostId] " +
                "for behandling=[$behandlingId] med bestillingId=[$bestillingId]"
        )
    }

    companion object {

        const val TYPE = "distribuerFrittståendeBrev"
    }
}
