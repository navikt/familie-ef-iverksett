package no.nav.familie.ef.iverksett.brev.frittstående

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.brev.BrevdistribusjonConflictExceptionResponseBody
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.kontrakter.felles.objectMapper
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
    private val frittståendeBrevRepository: FrittståendeBrevRepository,
    private val journalpostClient: JournalpostClient
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
        var frittståendeBrev = hentFrittståendeBrev(frittståendeBrevId)

        val journalpostResultat = frittståendeBrev.journalpostResultat.map
        val distribuertBrevResultat = frittståendeBrev.distribuerBrevResultat.map

        var resultat: Dødsbo? = null

        journalpostResultat.filter { (_, journalpostResultat) ->
            journalpostResultat.journalpostId !in distribuertBrevResultat
        }.forEach { (personIdent, journalpostResultat) ->
            try {
                val bestillingId = distribuerBrev(journalpostResultat)
                frittståendeBrev = lagreEndringerPåDistribuerbrevResultat(frittståendeBrev, journalpostResultat, bestillingId, frittståendeBrevId)
            } catch (e: RessursException) {
                val cause = e.cause
                if (cause is HttpClientErrorException.Gone) {
                    resultat = Dødsbo("Dødsbo personIdent=$personIdent ${cause.responseBodyAsString}")
                } else if (cause is HttpClientErrorException.Conflict) {
                    logger.warn("Conflict: Distribuering av frittstående brev allerede utført for journalpost: ${journalpostResultat.journalpostId} ")
                    with(objectMapper.readValue<BrevdistribusjonConflictExceptionResponseBody>(cause.responseBodyAsString)) {
                        lagreEndringerPåDistribuerbrevResultat(
                            frittståendeBrev,
                            journalpostResultat,
                            bestillingsId,
                            frittståendeBrevId
                        )
                    }
                    return OK
                } else {
                    throw e
                }
            }
        }
        return resultat ?: OK
    }

    private fun lagreEndringerPåDistribuerbrevResultat(
        frittståendeBrev: FrittståendeBrev,
        journalpostResultat: JournalpostResultat,
        bestillingId: String,
        frittståendeBrevId: UUID
    ): FrittståendeBrev {
        val frittståendeBrevOppdatert = oppdaterFrittståendeBrev(frittståendeBrev, journalpostResultat, bestillingId)
        frittståendeBrevRepository.oppdaterDistribuerBrevResultat(
                frittståendeBrevId,
                frittståendeBrevOppdatert.distribuerBrevResultat
        )
        return frittståendeBrevOppdatert
    }

    private fun oppdaterFrittståendeBrev(
        frittståendeBrev: FrittståendeBrev,
        journalpostResultat: JournalpostResultat,
        bestillingId: String
    ): FrittståendeBrev {
        val nyeVerdier = mapOf(journalpostResultat.journalpostId to DistribuerBrevResultat(bestillingId))
        val oppdaterteDistribuerBrevResultat = frittståendeBrev.distribuerBrevResultat.map + nyeVerdier
        return frittståendeBrev.copy(distribuerBrevResultat = DistribuerBrevResultatMap(oppdaterteDistribuerBrevResultat))
    }

    private fun distribuerBrev(
        journalpostResultat: JournalpostResultat
    ): String {
        val bestillingId = journalpostClient.distribuerBrev(journalpostResultat.journalpostId, Distribusjonstype.VIKTIG)
        loggBrevDistribuert(journalpostResultat.journalpostId, bestillingId)
        return bestillingId
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

    private fun hentFrittståendeBrev(frittståendeBrevId: UUID): FrittståendeBrev {
        val frittståendeBrev = frittståendeBrevRepository.findByIdOrThrow(frittståendeBrevId)
        if (frittståendeBrev.journalpostResultat.map.isEmpty()) {
            error("Fant ingen journalpost for frittståendeBrev=$frittståendeBrevId")
        }
        return frittståendeBrev
    }

    private fun loggBrevDistribuert(journalpostId: String, bestillingId: String) {
        logger.info(
            "Distribuer frittstående brev journalpost=[$journalpostId] " +
                "med bestillingId=[$bestillingId]"
        )
    }

    companion object {

        const val TYPE = "distribuerFrittståendeBrev"
    }
}
