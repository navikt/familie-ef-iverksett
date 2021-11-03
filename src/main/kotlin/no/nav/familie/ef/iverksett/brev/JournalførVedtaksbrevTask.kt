package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
        taskStepType = JournalførVedtaksbrevTask.TYPE,
        maxAntallFeil = 5,
        triggerTidVedFeilISekunder = 15,
        beskrivelse = "Journalfører vedtaksbrev."
)

class JournalførVedtaksbrevTask(private val iverksettingRepository: IverksettingRepository,
                                private val journalpostClient: JournalpostClient,
                                private val taskRepository: TaskRepository,
                                private val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)

        val vedtaksbrev = iverksettingRepository.hentBrev(behandlingId)
        val dokument = Dokument(vedtaksbrev.pdf,
                                Filtype.PDFA,
                                dokumenttype = Dokumenttype.VEDTAKSBREV_OVERGANGSSTØNAD,
                                tittel = lagDokumentTittel(iverksett.fagsak.stønadstype, iverksett.vedtak.vedtaksresultat))

        val journalpostId = journalpostClient.arkiverDokument(
                ArkiverDokumentRequest(
                        fnr = iverksett.søker.personIdent,
                        forsøkFerdigstill = true,
                        hoveddokumentvarianter = listOf(dokument),
                        fagsakId = iverksett.fagsak.eksternId.toString(),
                        journalførendeEnhet = iverksett.søker.tilhørendeEnhet,
                ),
                iverksett.vedtak.beslutterId
        ).journalpostId

        tilstandRepository.oppdaterJournalpostResultat(behandlingId = behandlingId,
                                                       JournalpostResultat(journalpostId = journalpostId)
        )
    }

    private fun lagDokumentTittel(stønadstype: StønadType, vedtaksresultat: Vedtaksresultat): String {
        return when (stønadstype) {
            StønadType.OVERGANGSSTØNAD ->
                lagVedtakstekstForOvergangsstønad(vedtaksresultat)
            StønadType.BARNETILSYN -> lagVedtakstekstForBarnetilsyn(vedtaksresultat)
            StønadType.SKOLEPENGER -> lagVedtakstekstForSkolepenger(vedtaksresultat)

        }

    }

    private fun lagVedtakstekstForSkolepenger(vedtaksresultat: Vedtaksresultat) =
            when (vedtaksresultat) {
                Vedtaksresultat.INNVILGET -> "Vedtak om innvilgelse av stønad til skolepenger"
                Vedtaksresultat.AVSLÅTT -> "Vedtak om avslag av stønad til skolepenger"
                Vedtaksresultat.OPPHØRT -> "Vedtak om opphør av stønad til skolepenger"
            }

    private fun lagVedtakstekstForBarnetilsyn(vedtaksresultat: Vedtaksresultat) =
            when (vedtaksresultat) {
                Vedtaksresultat.INNVILGET -> "Vedtak om innvilgelse av stønad til barnetilsyn"
                Vedtaksresultat.AVSLÅTT -> "Vedtak om avslag av stønad til barnetilsyn"
                Vedtaksresultat.OPPHØRT -> "Vedtak om opphør av stønad til barnetilsyn"
            }

    private fun lagVedtakstekstForOvergangsstønad(vedtaksresultat: Vedtaksresultat) =
            when (vedtaksresultat) {
                Vedtaksresultat.INNVILGET -> "Vedtak om innvilgelse av overgangsstønad"
                Vedtaksresultat.AVSLÅTT -> "Vedtak om avslag av overgangsstønad"
                Vedtaksresultat.OPPHØRT -> "Vedtak om opphør av overgangsstønad"
            }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "journalførVedtaksbrev"
    }
}