package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.KarakterutskriftBrev
import no.nav.familie.ef.iverksett.brev.stønadstypeTilDokumenttype
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
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
    taskStepType = JournalførKarakterutskriftBrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører frittstående brev for innhenting av karakterutskrift.",
)
class JournalførKarakterutskriftBrevTask(
    private val journalpostClient: JournalpostClient,
    private val taskService: TaskService,
    private val karakterutskriftBrevRepository: KarakterutskriftBrevRepository,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val brevId = UUID.fromString(task.payload)
        val brev = karakterutskriftBrevRepository.findByIdOrThrow(brevId)
        val dokumentRequest = opprettArkiverDokumentRequest(brev)

        val journalPostId =
            journalpostClient.arkiverDokument(
                arkiverDokumentRequest = dokumentRequest,
                saksbehandler = null,
            ).journalpostId

        karakterutskriftBrevRepository.update(brev.copy(journalpostId = journalPostId))
    }

    private fun opprettArkiverDokumentRequest(brev: KarakterutskriftBrev): ArkiverDokumentRequest {
        val eksternReferanseId = brev.eksternFagsakId.toString() + brev.gjeldendeÅr.toString() + brev.brevtype.name

        return ArkiverDokumentRequest(
            fnr = brev.personIdent,
            forsøkFerdigstill = true,
            hoveddokumentvarianter =
                listOf(
                    Dokument(
                        dokument = brev.fil,
                        filtype = Filtype.PDFA,
                        dokumenttype = stønadstypeTilDokumenttype(brev.stønadType),
                        tittel = brev.brevtype.tittel,
                    ),
                ),
            fagsakId = brev.eksternFagsakId.toString(),
            journalførendeEnhet = brev.journalførendeEnhet,
            eksternReferanseId = eksternReferanseId,
        )
    }

    override fun onCompletion(task: Task) {
        taskService.save(Task(DistribuerKarakterutskriftBrevTask.TYPE, task.payload, task.metadata))
    }

    companion object {
        const val TYPE = "JournalførKarakterutskriftBrevTask"
    }
}
