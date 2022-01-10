package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
        taskStepType = VentePåStatusFraØkonomiTask.TYPE,
        maxAntallFeil = 50,
        settTilManuellOppfølgning = true,
        triggerTidVedFeilISekunder = 30L,
        beskrivelse = "Sjekker status på utbetalningsoppdraget mot økonomi."
)

class VentePåStatusFraØkonomiTask(
        private val iverksettingRepository: IverksettingRepository,
        private val iverksettingService: IverksettingService,
        private val taskRepository: TaskRepository,
        private val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingId)
                             ?: error("Kunne ikke finne tilkjent ytelse for behandling=$behandlingId")

        if (tilkjentYtelse.harIngenUtbetalingsperioder()) {
            return
        }

        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(stønadstype = iverksett.fagsak.stønadstype,
                                                                     personIdent = iverksett.søker.personIdent,
                                                                     eksternBehandlingId = iverksett.behandling.eksternId,
                                                                     behandlingId = behandlingId)
    }

    override fun onCompletion(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)

        if (iverksett.erMigrering()) {
            logger.info("Journalfør ikke vedtaksbrev for behandling=$behandlingId då årsaken er migrering")
        } else {
            taskRepository.save(task.opprettNesteTask())
        }
    }

    companion object {

        const val TYPE = "sjekkStatusPåOppdrag"
    }

    fun TilkjentYtelse.harIngenUtbetalingsperioder() :Boolean {
        return this.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty()
        ?: error("Kunne ikke finne utbetalingsoppdrag for TilkjentYtelse")
    }

}