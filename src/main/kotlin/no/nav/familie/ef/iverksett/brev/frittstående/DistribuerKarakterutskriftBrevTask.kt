package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Loggtype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.RekjørSenereException
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = DistribuerKarakterutskriftBrevTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Distribuerer frittstående brev for innhenting av karakterutskrift.",
)
class DistribuerKarakterutskriftBrevTask(
    private val karakterutskriftBrevRepository: KarakterutskriftBrevRepository,
    private val journalpostClient: JournalpostClient,
    private val taskService: TaskService,
) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private sealed class Resultat
    private object OK : Resultat()
    private data class Dødsbo(val melding: String) : Resultat()

    override fun doTask(task: Task) {
        val brevId = UUID.fromString(task.payload)

        val resultat: Resultat = distribuerKarakterutskriftBrev(brevId)

        if (resultat is Dødsbo) {
            håndterDødsbo(task, resultat)
        }
    }

    private fun distribuerKarakterutskriftBrev(brevId: UUID): Resultat {
        val brev = karakterutskriftBrevRepository.findByIdOrThrow(brevId)
        val journalpostId = brev.journalpostId ?: throw IllegalStateException(
            "Distribuering av frittstående brev for innhenting av karakterutskrift " +
                "med id=$brevId feilet. Fant ingen journalpostId på brevet.",
        )

        try {
            distribuerBrev(journalpostId)
        } catch (e: RessursException) {
            val cause = e.cause
            when (cause) {
                is HttpClientErrorException.Gone ->
                    return Dødsbo("Dødsbo personIdent=${brev.personIdent} ${cause.responseBodyAsString}")
                is HttpClientErrorException.Conflict -> {
                    logger.warn("Conflict: Distribuering av frittstående brev allerede utført for journalpost: $journalpostId - lagrer betillingId: ${e.ressurs.data}")
                }
                else -> throw e
            }
        }
        return OK
    }

    private fun distribuerBrev(
        journalpostId: String,
    ): String {
        val bestillingId = journalpostClient.distribuerBrev(journalpostId, Distribusjonstype.VIKTIG)
        loggDistribuertBrev(journalpostId, bestillingId)
        return bestillingId
    }

    private fun håndterDødsbo(task: Task, dødsbo: Dødsbo) {
        val antallRekjørSenerePgaDødsbo = taskService.findTaskLoggByTaskId(task.id)
            .count { it.type == Loggtype.KLAR_TIL_PLUKK && it.melding?.startsWith("Dødsbo") == true }
        if (antallRekjørSenerePgaDødsbo < 7) {
            logger.warn("Mottaker for vedtaksbrev behandling=${task.payload} har dødsbo, prøver å sende brev på nytt om 7 dager")
            throw RekjørSenereException(dødsbo.melding, LocalDateTime.now().plusDays(7))
        } else {
            throw TaskExceptionUtenStackTrace("Er dødsbo og har feilet flere ganger: ${dødsbo.melding}")
        }
    }

    private fun loggDistribuertBrev(journalpostId: String, bestillingId: String) {
        logger.info(
            "Distribuerer frittstående brev for innhenting av karakterutskrift med " +
                "journalpostId=$journalpostId og bestillingId=$bestillingId",
        )
    }

    override fun onCompletion(task: Task) {
        taskService.save(Task(OppdaterKarakterinnhentingOppgaveTask.TYPE, task.payload, task.metadata))
    }

    companion object {

        const val TYPE = "distribuerKarakterutskriftBrev"
    }
}
