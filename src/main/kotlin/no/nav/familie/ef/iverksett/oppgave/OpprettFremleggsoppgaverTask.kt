package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.OppgaverForOpprettelse
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
    taskStepType = OpprettFremleggsoppgaverTask.TYPE,
    beskrivelse = "Oppretter fremleggsoppgave for overgangsstønad og barnetilsyn",
)
class OpprettFremleggsoppgaverTask(
    private val oppgaveService: OppgaveService,
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {
        const val TYPE = "opprettFremleggsoppgaver"
    }

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)
        if (iverksett.data is IverksettSkolepenger) {
            logger.info(
                "Oppretter ikke fremleggsoppgave for behandling=$behandlingId" +
                    " da det er en skolepenger (${iverksett::class.java.simpleName})",
            )
            return
        }

        opprettFremleggsoppgaveHvisOppgavetypeFinnes(OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID, iverksett.data, behandlingId)
        opprettFremleggsoppgaveHvisOppgavetypeFinnes(OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE, iverksett.data, behandlingId)
    }

    private fun opprettFremleggsoppgaveHvisOppgavetypeFinnes(
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
        iverksettData: IverksettData,
        behandlingId: UUID,
    ) {
        when (iverksettData) {
            is IverksettOvergangsstønad -> {
                if (iverksettData.vedtak.oppgaverForOpprettelse.oppgavetyper
                        .contains(oppgaveForOpprettelseType)
                ) {
                    val beskrivelse = lagBeskrivelseForFremleggsoppgave(oppgaveForOpprettelseType, iverksettData.vedtak.oppgaverForOpprettelse)
                    val oppgaveId = oppgaveService.opprettFremleggsoppgave(iverksettData, beskrivelse, oppgaveForOpprettelseType)
                    logger.info("Opprettet oppgave for behandling=$behandlingId oppgave=$oppgaveId")
                } else {
                    logger.info("Oppgave opprettes ikke for behandling=$behandlingId")
                }
            }

            is IverksettBarnetilsyn -> {
                if (iverksettData.vedtak.oppgaverForOpprettelse.oppgavetyper
                        .contains(oppgaveForOpprettelseType)
                ) {
                    val beskrivelse = lagBeskrivelseForFremleggsoppgave(oppgaveForOpprettelseType, iverksettData.vedtak.oppgaverForOpprettelse)
                    val oppgaveId = oppgaveService.opprettFremleggsoppgaveViaBarnetilsyn(iverksettData, beskrivelse, oppgaveForOpprettelseType)
                    logger.info("Opprettet oppgave for behandling=$behandlingId oppgave=$oppgaveId")
                } else {
                    logger.info("Oppgave opprettes ikke for behandling=$behandlingId")
                }
            }

            else -> {
                return
            }
        }
    }

    fun lagBeskrivelseForFremleggsoppgave(
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
        oppgaverForOpprettelse: OppgaverForOpprettelse,
    ): String {
        val årInntektskontrollAvSelvstendig = (
            oppgaverForOpprettelse.årForInntektskontrollSelvstendigNæringsdrivende
                ?.minus(1)
        )

        return when (oppgaveForOpprettelseType) {
            OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID -> "Inntekt"
            OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE -> "Kontroll av næringsinntekt for $årInntektskontrollAvSelvstendig"
        }
    }
}
