package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
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
    beskrivelse = "Journalfører brev for innhenting av karakterutskrift.",
)
class JournalførKarakterutskriftBrevTask(
    private val journalpostClient: JournalpostClient,
    private val taskService: TaskService,
    private val karakterutskriftBrevRepository: KarakterutskriftBrevRepository,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val callId = task.callId
        val brevId = UUID.fromString(task.payload)
        val brev = karakterutskriftBrevRepository.findByIdOrThrow(brevId)

        // TODO: Ikke hardkod dokumenttype
        journalpostClient.arkiverDokument(
            ArkiverDokumentRequest(
                fnr = brev.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(
                    Dokument(
                        dokument = brev.fil,
                        filtype = Filtype.PDFA,
                        dokumenttype = Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV,
                        tittel = brev.brevtype.tittel,
                    ),
                ),
                fagsakId = brev.eksternFagsakId.toString(),
                journalførendeEnhet = brev.journalførendeEnhet,
                eksternReferanseId = callId
            ),
            saksbehandler = null
        ).journalpostId
    }

    override fun onCompletion(task: Task) {
        // TODO: Distribuer brev
    }

    companion object {

        const val TYPE = "journalførKarakterutskriftBrev"
    }
}
