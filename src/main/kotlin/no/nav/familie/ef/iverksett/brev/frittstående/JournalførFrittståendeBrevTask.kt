package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.Brevmottaker
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.ef.iverksett.brev.domain.tilIdType
import no.nav.familie.ef.iverksett.brev.stønadstypeTilDokumenttype
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførFrittståendeBrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører frittstående brev."
)
class JournalførFrittståendeBrevTask(
    private val journalpostClient: JournalpostClient,
    private val taskService: TaskService,
    private val frittståendeBrevRepository: FrittståendeBrevRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val callId = task.callId
        val frittståendeBrevId = UUID.fromString(task.payload)
        var frittståendeBrev = frittståendeBrevRepository.findByIdOrThrow(frittståendeBrevId)

        frittståendeBrev.mottakere.mottakere
            .forEachIndexed { index, brevmottaker ->
                if (frittståendeBrev.journalpostResulat.map.containsKey(brevmottaker.ident)) {
                    return@forEachIndexed
                }
                val journalpostId = journalpostClient.arkiverDokument(
                    opprettArkiverDokumentRequest(frittståendeBrev, callId, index, brevmottaker),
                    saksbehandler = frittståendeBrev.saksbehandlerIdent
                ).journalpostId
                val oppdatertJournalposter =
                    frittståendeBrev.journalpostResulat.map + mapOf(brevmottaker.ident to JournalpostResultat(journalpostId))
                frittståendeBrev = frittståendeBrev.copy(journalpostResulat = JournalpostResultatMap(oppdatertJournalposter))
                frittståendeBrevRepository.oppdaterJournalpostResultat(frittståendeBrevId, frittståendeBrev.journalpostResulat)
            }
    }

    override fun onCompletion(task: Task) {
        taskService.save(Task(DistribuerFrittståendeBrevTask.TYPE, task.payload, task.metadata))
    }

    private fun opprettArkiverDokumentRequest(
        frittståendeBrev: FrittståendeBrev,
        callId: String,
        index: Int,
        brevmottaker: Brevmottaker
    ) = ArkiverDokumentRequest(
        fnr = frittståendeBrev.personIdent,
        forsøkFerdigstill = true,
        hoveddokumentvarianter = listOf(
            Dokument(
                frittståendeBrev.fil,
                Filtype.PDFA,
                dokumenttype = stønadstypeTilDokumenttype(frittståendeBrev.stønadstype),
                tittel = frittståendeBrev.brevtype.tittel
            )
        ),
        fagsakId = frittståendeBrev.eksternFagsakId.toString(),
        journalførendeEnhet = frittståendeBrev.journalførendeEnhet,
        eksternReferanseId = "$callId-$index",
        avsenderMottaker = avsenderMottaker(frittståendeBrev, brevmottaker)
    )

    private fun avsenderMottaker(
        frittståendeBrev: FrittståendeBrev,
        brevmottaker: Brevmottaker
    ) =
        if (frittståendeBrev.personIdent != brevmottaker.ident) AvsenderMottaker(
            brevmottaker.ident,
            brevmottaker.identType.tilIdType(),
            brevmottaker.navn
        ) else null

    companion object {

        const val TYPE = "journalførFrittståendeBrev"
    }
}
