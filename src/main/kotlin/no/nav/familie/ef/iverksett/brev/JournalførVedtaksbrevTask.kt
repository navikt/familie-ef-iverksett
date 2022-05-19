package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
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

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {

        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)

        if ((iverksett.vedtak.brevmottakere?.mottakere?.isNotEmpty() == true) && !featureToggleService.isEnabled("familie.ef.iverksett.brevmottakere")) {
            error("Toggle for journalføring til brevmottakere er ikke påskrudd")
        }

        journalførVedtaksbrev(behandlingId, iverksett)

        validerJournalpostResultatErSatt(behandlingId, iverksett)

    }

    private fun journalførOgLagreResultat(behandlingId: UUID,
                                          arkiverDokumentRequest: ArkiverDokumentRequest,
                                          mottakerIdent: String,
                                          beslutterId: String) {

        val journalpostId = try {
            journalpostClient.arkiverDokument(arkiverDokumentRequest, beslutterId).journalpostId
        } catch (e: RessursException) {
            if (e.cause is HttpClientErrorException.Conflict) {
                val eksternReferanseId = arkiverDokumentRequest.eksternReferanseId
                logger.warn("Konflikt ved arkivering av dokument, " +
                            "prøver å hente journalpostId med eksternReferanseId=$eksternReferanseId")
                finnJournalpostIdForEksternReferanseId(mottakerIdent, eksternReferanseId)
            } else {
                throw e
            }
        }
        tilstandRepository.oppdaterJournalpostResultat(
                behandlingId = behandlingId,
                mottakerIdent = mottakerIdent,
                JournalpostResultat(journalpostId = journalpostId)
        )
    }

    private fun finnJournalpostIdForEksternReferanseId(mottakerIdent: String,
                                                       eksternReferanseId: String?): String {
        val request = JournalposterForBrukerRequest(Bruker(mottakerIdent, BrukerIdType.FNR), 50, listOf(Tema.ENF))
        return journalpostClient.finnJournalposter(request)
                       .find { it.eksternReferanseId == eksternReferanseId }
                       ?.journalpostId
               ?: error("Finner ikke journalpost med eksternReferanseId=$eksternReferanseId")
    }

    private fun journalførVedtaksbrev(behandlingId: UUID,
                                      iverksett: Iverksett) {
        val vedtaksbrev = iverksettingRepository.hentBrev(behandlingId)
        val dokument = Dokument(vedtaksbrev.pdf,
                                Filtype.PDFA,
                                dokumenttype = vedtaksbrevForStønadType(iverksett.fagsak.stønadstype),
                                tittel = lagDokumentTittel(iverksett))


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

        if (iverksett.vedtak.brevmottakere?.mottakere?.isEmpty() == true) {
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
                val arkiverDokumentRequestForMottaker = arkiverDokumentRequest.copy(
                        eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$indeks",
                        avsenderMottaker = AvsenderMottaker(
                                id = mottaker.ident,
                                idType = mottaker.identType.tilIdType(),
                                navn = mottaker.navn
                        )
                )

                journalførOgLagreResultat(behandlingId = behandlingId,
                                          arkiverDokumentRequest = arkiverDokumentRequestForMottaker,
                                          mottakerIdent = mottaker.ident,
                                          beslutterId = iverksett.vedtak.beslutterId)
            }
        }
    }

    private fun journalførVedtaksbrevTilStønadmottaker(arkiverDokumentRequest: ArkiverDokumentRequest,
                                                       iverksett: Iverksett,
                                                       behandlingId: UUID) {
        journalførOgLagreResultat(behandlingId = behandlingId,
                                  arkiverDokumentRequest = arkiverDokumentRequest,
                                  mottakerIdent = iverksett.søker.personIdent,
                                  beslutterId = iverksett.vedtak.beslutterId)
    }

    private fun validerJournalpostResultatErSatt(behandlingId: UUID, iverksett: Iverksett) {
        val antallJournalføringer = tilstandRepository.hentJournalpostResultat(behandlingId)?.size
                                    ?: error("Ingen journalføringer for behandling=[$behandlingId]")

        val satteBrevmottakere = iverksett.vedtak.brevmottakere?.mottakere?.size ?: 0

        // Hvis ingen brevmottakere er satt skal bare stønadsmottaker ha vedtaksbrevet
        val forventetJournalføringer = if (satteBrevmottakere > 0) satteBrevmottakere else 1

        if (forventetJournalføringer != antallJournalføringer) {
            error("Feil ved journalføring av vedtaksbrev. Forventet $forventetJournalføringer journalføringsreultat, fant $antallJournalføringer.")
        }
    }


    private fun lagDokumentTittel(iverksett: Iverksett): String =
            lagVedtakstekst(iverksett) + lagStønadtypeTekst(iverksett.fagsak.stønadstype)

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