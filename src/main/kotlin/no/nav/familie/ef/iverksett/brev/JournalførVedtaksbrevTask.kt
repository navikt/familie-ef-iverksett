package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.brev.domain.tilIdType
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev.",
)
class JournalførVedtaksbrevTask(
    private val iverksettingRepository: IverksettingRepository,
    private val journalpostClient: JournalpostClient,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)

        journalførVedtaksbrev(behandlingId, iverksett)

        validerJournalpostResultatErSatt(behandlingId, iverksett.data)
    }

    private fun journalførOgLagreResultat(
        behandlingId: UUID,
        arkiverDokumentRequest: ArkiverDokumentRequest,
        mottakerIdent: String,
        beslutterId: String,
    ) {
        val journalpostId =
            try {
                journalpostClient.arkiverDokument(arkiverDokumentRequest, beslutterId).journalpostId
            } catch (e: RessursException) {
                if (e.cause is HttpClientErrorException.Conflict) {
                    val eksternReferanseId = arkiverDokumentRequest.eksternReferanseId
                    logger.warn(
                        "Konflikt ved arkivering av dokument, " +
                            "prøver å hente journalpostId med eksternReferanseId=$eksternReferanseId",
                    )
                    finnJournalpostIdForEksternReferanseId(mottakerIdent, eksternReferanseId)
                } else {
                    throw e
                }
            }
        iverksettResultatService.oppdaterJournalpostResultat(
            behandlingId = behandlingId,
            mottakerIdent = mottakerIdent,
            JournalpostResultat(journalpostId = journalpostId),
        )
    }

    private fun finnJournalpostIdForEksternReferanseId(
        mottakerIdent: String,
        eksternReferanseId: String?,
    ): String {
        val request = JournalposterForBrukerRequest(Bruker(mottakerIdent, BrukerIdType.FNR), 50, listOf(Tema.ENF))
        return journalpostClient.finnJournalposter(request)
            .find { it.eksternReferanseId == eksternReferanseId }
            ?.journalpostId
            ?: error("Finner ikke journalpost med eksternReferanseId=$eksternReferanseId")
    }

    private fun journalførVedtaksbrev(
        behandlingId: UUID,
        iverksett: Iverksett,
    ) {
        val vedtaksbrev = iverksett.brev ?: error("Fant ikke brev for behandlingId : $behandlingId")
        val dokument =
            Dokument(
                vedtaksbrev.pdf,
                Filtype.PDFA,
                dokumenttype = vedtaksbrevForStønadType(iverksett.data.fagsak.stønadstype),
                tittel = lagDokumentTittel(iverksett.data),
            )

        val arkiverDokumentRequest =
            ArkiverDokumentRequest(
                fnr = iverksett.data.søker.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(dokument),
                vedleggsdokumenter = if (vedleggForRettigheter(iverksett)) hentVedleggForRettigheter(iverksett.data.fagsak.stønadstype) else emptyList(),
                fagsakId = iverksett.data.fagsak.eksternId.toString(),
                journalførendeEnhet = iverksett.data.søker.tilhørendeEnhet,
                eksternReferanseId = "$behandlingId-vedtaksbrev",
            )

        val journalførteIdenter: List<String> =
            iverksettResultatService.hentJournalpostResultat(behandlingId)?.keys?.toList() ?: emptyList()

        if (iverksett.data.vedtak.brevmottakere?.mottakere?.isEmpty() == true) {
            journalførVedtaksbrevTilStønadmottaker(arkiverDokumentRequest, iverksett.data, behandlingId)
        } else {
            journalførVedtaksbrevTilBrevmottakere(iverksett.data, journalførteIdenter, arkiverDokumentRequest, behandlingId)
        }
    }

    private fun vedleggForRettigheter(iverksett: Iverksett): Boolean {
        return when (iverksett.data.behandling.behandlingÅrsak) {
            BehandlingÅrsak.G_OMREGNING -> false
            BehandlingÅrsak.MIGRERING -> false
            BehandlingÅrsak.SATSENDRING -> false
            BehandlingÅrsak.SANKSJON_1_MND -> false
            BehandlingÅrsak.KORRIGERING_UTEN_BREV -> false
            else -> iverksett.data.vedtak.vedtaksresultat == Vedtaksresultat.INNVILGET
        }
    }

    private fun hentVedleggForRettigheter(stønadType: StønadType): List<Dokument> {
        val pdf = lesPdfForVedleggForRettigheter(stønadType)
        return listOf(
            Dokument(
                pdf,
                Filtype.PDFA,
                dokumenttype = stønadstypeTilDokumenttype(stønadType),
                tittel = vedleggForRettigheterTittelTekst(),
                filnavn = utledFilnavnForVedleggAvRettigheter(stønadType),
            ),
        )
    }

    private fun journalførVedtaksbrevTilBrevmottakere(
        iverksett: IverksettData,
        journalførteIdenter: List<String>,
        arkiverDokumentRequest: ArkiverDokumentRequest,
        behandlingId: UUID,
    ) {
        iverksett.vedtak.brevmottakere?.mottakere?.mapIndexed { indeks, mottaker ->
            if (!journalførteIdenter.contains(mottaker.ident)) {
                val arkiverDokumentRequestForMottaker =
                    arkiverDokumentRequest.copy(
                        eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$indeks",
                        avsenderMottaker =
                            AvsenderMottaker(
                                id = mottaker.ident,
                                idType = mottaker.identType.tilIdType(),
                                navn = mottaker.navn,
                            ),
                    )

                journalførOgLagreResultat(
                    behandlingId = behandlingId,
                    arkiverDokumentRequest = arkiverDokumentRequestForMottaker,
                    mottakerIdent = mottaker.ident,
                    beslutterId = iverksett.vedtak.beslutterId,
                )
            }
        }
    }

    private fun journalførVedtaksbrevTilStønadmottaker(
        arkiverDokumentRequest: ArkiverDokumentRequest,
        iverksett: IverksettData,
        behandlingId: UUID,
    ) {
        journalførOgLagreResultat(
            behandlingId = behandlingId,
            arkiverDokumentRequest = arkiverDokumentRequest,
            mottakerIdent = iverksett.søker.personIdent,
            beslutterId = iverksett.vedtak.beslutterId,
        )
    }

    private fun validerJournalpostResultatErSatt(
        behandlingId: UUID,
        iverksett: IverksettData,
    ) {
        val antallJournalføringer =
            iverksettResultatService.hentJournalpostResultat(behandlingId)?.size
                ?: error("Ingen journalføringer for behandling=[$behandlingId]")

        val satteBrevmottakere = iverksett.vedtak.brevmottakere?.mottakere?.size ?: 0

        // Hvis ingen brevmottakere er satt skal bare stønadsmottaker ha vedtaksbrevet
        val forventetJournalføringer = if (satteBrevmottakere > 0) satteBrevmottakere else 1

        if (forventetJournalføringer != antallJournalføringer) {
            error(
                "Feil ved journalføring av vedtaksbrev. Forventet $forventetJournalføringer journalføringsreultat, " +
                    "fant $antallJournalføringer.",
            )
        }
    }

    private fun lagDokumentTittel(iverksett: IverksettData): String =
        lagVedtakstekst(iverksett) + lagStønadtypeTekst(iverksett.fagsak.stønadstype)

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNesteTask())
    }

    companion object {
        const val TYPE = "journalførVedtaksbrev"
    }
}
