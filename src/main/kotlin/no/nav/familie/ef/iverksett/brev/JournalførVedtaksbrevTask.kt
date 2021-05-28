package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.IverksettingDbUtil
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandDbUtil
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev."
)

class JournalførVedtaksbrevTask(val iverksettingDbUtil: IverksettingDbUtil,
                                val journalpostClient: JournalpostClient,
                                val taskRepository: TaskRepository,
                                val tilstandDbUtil: TilstandDbUtil
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingDbUtil.hentIverksett(behandlingId)

        val vedtaksbrev = iverksettingDbUtil.hentBrev(behandlingId)
        val dokument = Dokument(vedtaksbrev.pdf, Filtype.PDFA, dokumenttype = Dokumenttype.VEDTAKSBREV_OVERGANGSSTØNAD)

        val journalpostId = journalpostClient.arkiverDokument(
            ArkiverDokumentRequest(
                fnr = iverksett.søker.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(dokument),
                fagsakId = iverksett.fagsak.eksternId.toString(),
                journalførendeEnhet = iverksett.søker.tilhørendeEnhet
            )
        ).journalpostId

        tilstandDbUtil.lagreJournalPostResultat(behandlingId = behandlingId,
                                                JournalpostResultat(journalpostId = journalpostId)
        )
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {
        const val TYPE = "journalførVedtaksbrev"
    }
}