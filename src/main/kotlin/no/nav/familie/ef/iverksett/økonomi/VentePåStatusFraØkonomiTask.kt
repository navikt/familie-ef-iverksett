package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = VentePåStatusFraØkonomiTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 30L,
    beskrivelse = "Sjekker status på utbetalningsoppdraget mot økonomi.",
)
class VentePåStatusFraØkonomiTask(
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingId)
            ?: error("Kunne ikke finne tilkjent ytelse for behandling=$behandlingId")

        if (tilkjentYtelse.harIngenUtbetalingsperioder()) {
            return
        }

        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(
            stønadstype = iverksett.fagsak.stønadstype,
            personIdent = iverksett.søker.personIdent,
            eksternBehandlingId = iverksett.behandling.eksternId,
            behandlingId = behandlingId,
        )
    }

    override fun onCompletion(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data

        if (iverksett.skalIkkeSendeBrev()) {
            logger.info(
                "Journalfør ikke vedtaksbrev for behandling=$behandlingId då årsak=${iverksett.behandling.behandlingÅrsak}",
            )
        } else {
            taskService.save(task.opprettNesteTask())
        }
    }

    companion object {

        const val TYPE = "sjekkStatusPåOppdrag"
    }

    fun TilkjentYtelse.harIngenUtbetalingsperioder(): Boolean {
        return this.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty()
            ?: error("Kunne ikke finne utbetalingsoppdrag for TilkjentYtelse")
    }
}
