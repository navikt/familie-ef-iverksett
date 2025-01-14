package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.AktivitetspliktBrevRepository
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.JournalførAktivitetspliktutskriftBrevTask
import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.domain.tilDomene
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.ef.felles.PeriodiskAktivitetspliktBrevDto
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FrittståendeBrevService(
    private val frittståendeBrevRepository: FrittståendeBrevRepository,
    private val aktivitetspliktBrevRepository: AktivitetspliktBrevRepository,
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun opprettTask(data: FrittståendeBrevDto) {
        val mottakere = data.mottakere
        logger.info("Oppretter task for frittstående brev for fagsak=${data.eksternFagsakId}")
        if (mottakere.isNullOrEmpty()) {
            throw IllegalArgumentException("Liste med brevmottakere kan ikke være tom")
        }

        val brev =
            frittståendeBrevRepository.insert(
                FrittståendeBrev(
                    personIdent = data.personIdent,
                    eksternFagsakId = data.eksternFagsakId,
                    journalførendeEnhet = data.journalførendeEnhet,
                    saksbehandlerIdent = data.saksbehandlerIdent,
                    stønadstype = data.stønadType,
                    mottakere = Brevmottakere(mottakere.map { it.toDomain() }),
                    fil = data.fil,
                    brevtype = data.brevtype,
                    tittel = data.tittel,
                ),
            )
        taskService.save(Task(JournalførFrittståendeBrevTask.TYPE, brev.id.toString()))
    }

    @Transactional
    fun opprettTaskForInnhentingAvAktivitetsplikt(brevDto: PeriodiskAktivitetspliktBrevDto) {
        validerKanLagreAktivitetspliktutskriftBrev(brevDto)

        val brev = aktivitetspliktBrevRepository.insert(brevDto.tilDomene())

        taskService.save(Task(JournalførAktivitetspliktutskriftBrevTask.TYPE, brev.id.toString()))
    }

    private fun validerKanLagreAktivitetspliktutskriftBrev(brevDto: PeriodiskAktivitetspliktBrevDto) {
        if (aktivitetspliktBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(
                brevDto.eksternFagsakId,
                brevDto.oppgaveId,
                brevDto.gjeldendeÅr,
            )
        ) {
            throw ApiFeil(
                "Skal ikke kunne opprette flere innhentingsbrev for fagsak med eksternId=${brevDto.eksternFagsakId}",
                HttpStatus.BAD_REQUEST,
            )
        }
        if (aktivitetspliktBrevRepository.existsByEksternFagsakIdAndGjeldendeÅr(
                brevDto.eksternFagsakId,
                brevDto.gjeldendeÅr,
            )
        ) {
            throw ApiFeil(
                "Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=${brevDto.eksternFagsakId}",
                HttpStatus.BAD_REQUEST,
            )
        }
    }
}
