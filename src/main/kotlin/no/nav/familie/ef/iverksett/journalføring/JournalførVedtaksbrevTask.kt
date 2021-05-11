package no.nav.familie.ef.iverksett.journalføring

import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev."
)

class JournalførVedtaksbrevTask(val hentIverksettService: HentIverksettService, val journalpostClient: JournalpostClient) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = hentIverksettService.hentIverksett(behandlingId.toString())

        val vedtaksbrev = hentIverksettService.hentBrev(behandlingId.toString())
        val dokument = Dokument(vedtaksbrev.pdf, Filtype.PDFA, dokumenttype = Dokumenttype.VEDTAKSBREV_OVERGANGSSTØNAD)

        val journalpostId = journalpostClient.arkiverDokument(
            ArkiverDokumentRequest(
                fnr = iverksett.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(dokument),
                fagsakId = iverksett.saksnummer,
                journalførendeEnhet = iverksett.tilhørendeEnhet
            )
        ).journalpostId

        // Legg til behandlingjournalpost ?
    }

    override fun onCompletion(task: Task) {
        // Lag distribuer vedtaksbrev task
    }

    companion object {

        fun opprettTask(iverksett: Iverksett): Task =
            Task(
                type = TYPE,
                payload = iverksett.behandlingId
            )

        const val TYPE = "journalførVedtaksbrev"
    }


}