package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForOpprettelseType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettFremleggsoppgaveViaBarnetilsynTask.TYPE,
    beskrivelse = "Oppretter fremleggsoppgave via barnetilsyn",
)
class OpprettFremleggsoppgaveViaBarnetilsynTask(
    private val oppgaveService: OppgaveService,
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)
        if (iverksett.data !is IverksettBarnetilsyn) {
            logger.info(
                "Oppretter ikke fremleggsoppgave for behandling=$behandlingId" +
                    " da det ikke er en barnetilsyn (${iverksett::class.java.simpleName})",
            )
            return
        }

        opprettFremleggsoppgaveHvisOppgavetypeFinnes(OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID, iverksett.data, behandlingId)
        opprettFremleggsoppgaveHvisOppgavetypeFinnes(OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE, iverksett.data, behandlingId)
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {
        const val TYPE = "opprettFremleggsoppgaveViaBarnetilsyn"
    }

    private fun opprettFremleggsoppgaveHvisOppgavetypeFinnes(
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
        iverksettData: IverksettBarnetilsyn,
        behandlingId: UUID,
    ) {
        if (iverksettData.vedtak.oppgaverForOpprettelse.oppgavetyper.contains(
                oppgaveForOpprettelseType,
            )
        ) {
            val beskrivelse = lagBeskrivelseForFremleggsoppgave(oppgaveForOpprettelseType, iverksettData)

            val oppgaveId = oppgaveService.opprettFremleggsoppgaveViaBarnetilsyn(iverksettData, beskrivelse, oppgaveForOpprettelseType)
            logger.info("Opprettet oppgave for behandling=$behandlingId oppgave=$oppgaveId med barnetilsyn")
        } else {
            logger.info("Oppgave opprettes ikke for behandling=$behandlingId med barnetilsyn")
        }
    }

    fun lagBeskrivelseForFremleggsoppgave(
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
        iverksettData: IverksettBarnetilsyn,
    ): String {
        val årInntektskontrollAvSelvstendig = (
            iverksettData.vedtak.oppgaverForOpprettelse.årForInntektskontrollSelvstendigNæringsdrivende
                ?.minus(1)
        )

        return when (oppgaveForOpprettelseType) {
            OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID -> "Inntekt"
            OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE -> "Kontroll av næringsinntekt for $årInntektskontrollAvSelvstendig"
        }
    }
}
