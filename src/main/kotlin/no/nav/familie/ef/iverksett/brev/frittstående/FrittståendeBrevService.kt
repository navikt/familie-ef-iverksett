package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.domain.tilDomene
import no.nav.familie.ef.iverksett.brev.stønadstypeTilDokumenttype
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.ef.felles.KarakterutskriftBrevDto
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FrittståendeBrevService(
    private val frittståendeBrevRepository: FrittståendeBrevRepository,
    private val karakterutskriftBrevRepository: KarakterutskriftBrevRepository,
    private val taskService: TaskService,
    private val journalpostClient: JournalpostClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun journalførOgDistribuerBrev(data: FrittståendeBrevDto) {
        val journalpostId = journalpostClient.arkiverDokument(
            ArkiverDokumentRequest(
                fnr = data.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(
                    Dokument(
                        data.fil,
                        Filtype.PDFA,
                        dokumenttype = stønadstypeTilDokumenttype(data.stønadType),
                        tittel = data.brevtype.tittel,
                    ),
                ),
                fagsakId = data.eksternFagsakId.toString(),
                journalførendeEnhet = data.journalførendeEnhet,
            ),
            data.saksbehandlerIdent,
        ).journalpostId
        try {
            val bestillingId = journalpostClient.distribuerBrev(journalpostId, Distribusjonstype.VIKTIG)
            logger.info("Sendt frittstående brev journalpost=$journalpostId bestillingId=$bestillingId")
        } catch (e: Exception) {
            logger.error("Feilet distribuering av $journalpostId")
            secureLogger.error("Feilet distribuering av $journalpostId", e)
            // kaster ApiFeil som ikke logger då vi allerede logger feilet
            throw ApiFeil("Feilet journalføring av $journalpostId", HttpStatus.BAD_REQUEST)
        }
    }

    @Transactional
    fun opprettTask(data: FrittståendeBrevDto) {
        val mottakere = data.mottakere
        if (mottakere == null || mottakere.isEmpty()) {
            throw IllegalArgumentException("Liste med brevmottakere kan ikke være tom")
        }

        val brev = frittståendeBrevRepository.insert(
            FrittståendeBrev(
                personIdent = data.personIdent,
                eksternFagsakId = data.eksternFagsakId,
                journalførendeEnhet = data.journalførendeEnhet,
                saksbehandlerIdent = data.saksbehandlerIdent,
                stønadstype = data.stønadType,
                mottakere = Brevmottakere(mottakere.map { it.toDomain() }),
                fil = data.fil,
                brevtype = data.brevtype,
            ),
        )
        taskService.save(Task(JournalførFrittståendeBrevTask.TYPE, brev.id.toString()))
    }

    @Transactional
    fun opprettTask(brevDto: KarakterutskriftBrevDto) {
        validerKanLagreKarakterutskriftBrev(brevDto)

        val brev = karakterutskriftBrevRepository.insert(brevDto.tilDomene())

        taskService.save(Task(JournalførKarakterutskriftBrevTask.TYPE, brev.id.toString()))
    }

    private fun validerKanLagreKarakterutskriftBrev(brevDto: KarakterutskriftBrevDto) {
        if (
            brevDto.brevtype != FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE &&
            brevDto.brevtype != FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_UTVIDET_PERIODE
        ) {
            throw ApiFeil("Skal ikke opprette automatiske innhentingsbrev for frittstående brev av type ${brevDto.brevtype}", HttpStatus.BAD_REQUEST)
        }
        if (karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(
                brevDto.eksternFagsakId,
                brevDto.oppgaveId,
                brevDto.gjeldendeÅr,
            )
        ) {
            throw ApiFeil("Skal ikke kunne opprette flere innhentingsbrev for fagsak med eksternId=${brevDto.eksternFagsakId}", HttpStatus.BAD_REQUEST)
        }
        if (karakterutskriftBrevRepository.existsByEksternFagsakIdAndGjeldendeÅrAndBrevtype(
                brevDto.eksternFagsakId,
                brevDto.gjeldendeÅr,
                brevDto.brevtype,
            )
        ) {
            throw ApiFeil("Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=${brevDto.eksternFagsakId}", HttpStatus.BAD_REQUEST)
        }
    }
}
