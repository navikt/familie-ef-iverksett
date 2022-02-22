package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
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
                                private val tilstandRepository: TilstandRepository,
                                private val featureToggleService: FeatureToggleService
) : AsyncTaskStep {

    override fun doTask(task: Task) {

        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)

        if ((iverksett.vedtak.brevmottakere?.mottakere?.isNotEmpty() == true) && !featureToggleService.isEnabled("familie.ef.iverksett.brevmottakere")) {
            error("Toggle for journalføring til brevmottakere er ikke påskrudd")
        }

        journalførVedtaksbrev(behandlingId, iverksett)

        validerJournalpostResultatErSatt(behandlingId)

    }

    private fun journalførVedtaksbrev(behandlingId: UUID,
                                      iverksett: Iverksett) {
        val vedtaksbrev = iverksettingRepository.hentBrev(behandlingId)
        val dokument = Dokument(vedtaksbrev.pdf,
                                Filtype.PDFA,
                                dokumenttype = vedtaksbrevForStønadType(iverksett.fagsak.stønadstype),
                                tittel = lagDokumentTittel(iverksett.fagsak.stønadstype, iverksett.vedtak.vedtaksresultat))


        val arkiverDokumentRequest = ArkiverDokumentRequest(
                fnr = iverksett.søker.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(dokument),
                fagsakId = iverksett.fagsak.eksternId.toString(),
                journalførendeEnhet = iverksett.søker.tilhørendeEnhet,
                eksternReferanseId = "$behandlingId-vedtaksbrev"
        )

        val journalførteIdenter: List<String> =
                tilstandRepository.hentJournalpostResultat(behandlingId)?.keys?.toList() ?: emptyList()

        if(iverksett.vedtak.brevmottakere?.mottakere?.isEmpty() == true){
            journalførVedtaksbrevTilStønadmottaker(arkiverDokumentRequest, iverksett, behandlingId)
        } else {
            journalførVedtaksbrevTilBrevmottakere(iverksett, journalførteIdenter, arkiverDokumentRequest, behandlingId)
        }
    }

    private fun journalførVedtaksbrevTilBrevmottakere(iverksett: Iverksett,
                                                      journalførteIdenter: List<String>,
                                                      arkiverDokumentRequest: ArkiverDokumentRequest,
                                                      behandlingId: UUID) {
        iverksett.vedtak.brevmottakere?.mottakere?.mapIndexed { indeks, mottaker ->
            if (!journalførteIdenter.contains(mottaker.ident)) {
                val journalpostId = journalpostClient.arkiverDokument(
                        arkiverDokumentRequest.copy(
                                eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$indeks",
                                avsenderMottaker = AvsenderMottaker(
                                        id = mottaker.ident,
                                        idType = mottaker.identType.tilIdType(),
                                        navn = mottaker.navn
                                )
                        ),
                        iverksett.vedtak.beslutterId
                ).journalpostId

                tilstandRepository.oppdaterJournalpostResultat(
                        behandlingId = behandlingId,
                        mottakerIdent = mottaker.ident,
                        JournalpostResultat(journalpostId = journalpostId)
                )
            }
        }
    }

    private fun journalførVedtaksbrevTilStønadmottaker(arkiverDokumentRequest: ArkiverDokumentRequest,
                                                       iverksett: Iverksett,
                                                       behandlingId: UUID) {
        val journalpostId = journalpostClient.arkiverDokument(
                arkiverDokumentRequest,
                iverksett.vedtak.beslutterId
        ).journalpostId

        tilstandRepository.oppdaterJournalpostResultat(
                behandlingId = behandlingId,
                mottakerIdent = iverksett.søker.personIdent,
                journalPostResultat = JournalpostResultat(journalpostId = journalpostId)
        )
    }

    private fun validerJournalpostResultatErSatt(behandlingId: UUID) {
        val journalpostResultat = tilstandRepository.hentJournalpostResultat(behandlingId)
        if(journalpostResultat.isNullOrEmpty()){
            error("Ingen journalføringer er gjort")
        }
    }


    private fun lagDokumentTittel(stønadstype: StønadType, vedtaksresultat: Vedtaksresultat): String =
            lagVedtakstekst(vedtaksresultat) + lagStønadtypeTekst(stønadstype)

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "journalførVedtaksbrev"
    }

    private fun Brevmottaker.IdentType.tilIdType(): BrukerIdType =
        when (this) {
            Brevmottaker.IdentType.ORGANISASJONSNUMMER -> BrukerIdType.ORGNR
            Brevmottaker.IdentType.PERSONIDENT -> BrukerIdType.FNR
        }

}