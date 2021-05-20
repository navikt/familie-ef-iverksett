package no.nav.familie.ef.iverksett.journalføring

import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
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

class JournalførVedtaksbrevTask(val hentIverksettService: HentIverksettService,
                                val journalpostClient: JournalpostClient,
                                val taskRepository: TaskRepository,
                                val lagreTilstandService: LagreTilstandService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = hentIverksettService.hentIverksett(behandlingId)

        val vedtaksbrev = hentIverksettService.hentBrev(behandlingId)
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

        lagreTilstandService.lagreJournalPostResultat(behandlingId = behandlingId,
                                                      JournalpostResultat(journalpostId = journalpostId)
        )

        lagDistribuerVedtaksbrevTask(behandlingId, journalpostId)
    }

    private fun lagDistribuerVedtaksbrevTask(behandlingId: UUID, journalpostId: String) {
        taskRepository.save(DistribuerVedtaksbrevTask.opprettTask(behandlingId, journalpostId))
    }

    companion object {
        const val TYPE = "journalførVedtaksbrev"
    }
}