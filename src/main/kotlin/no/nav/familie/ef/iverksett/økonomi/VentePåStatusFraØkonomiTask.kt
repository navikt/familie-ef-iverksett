package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
        taskStepType = VentePåStatusFraØkonomiTask.TYPE,
        maxAntallFeil = 50,
        settTilManuellOppfølgning = true,
        triggerTidVedFeilISekunder = 5 * 60L,
        beskrivelse = "Sjekker status på utbetalningsoppdraget mot økonomi."
)

class VentePåStatusFraØkonomiTask(
        val iverksettingRepository: IverksettingRepository,
        val iverksettingService: IverksettingService,
        val taskRepository: TaskRepository,
        val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(stønadstype = iverksett.fagsak.stønadstype,
                                                                     personIdent = iverksett.søker.personIdent,
                                                                     eksternBehandlingId = iverksett.behandling.eksternId,
                                                                     behandlingId = behandlingId)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "sjekkStatusPåOppdrag"
    }
}