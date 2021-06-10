package no.nav.familie.ef.iverksett.tekniskopphor

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(
        taskStepType = VentePåStatusFørTekniskOpphørTask.TYPE,
        maxAntallFeil = 50,
        settTilManuellOppfølgning = true,
        triggerTidVedFeilISekunder = 5 * 60L,
        beskrivelse = "Sjekker status på teknisk opphør mot økonomi."
)

class VentePåStatusFørTekniskOpphørTask(
        val iverksettingRepository: IverksettingRepository,
        val iverksettingService: IverksettingService,
        val taskRepository: TaskRepository,
        val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val tekniskOpphør = iverksettingRepository.hentTekniskOpphør(behandlingId)
        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(stønadstype = tekniskOpphør.tilkjentYtelseMedMetaData.stønadstype,
                                                                     personIdent = tekniskOpphør.tilkjentYtelseMedMetaData.personIdent,
                                                                     eksternBehandlingId = tekniskOpphør.tilkjentYtelseMedMetaData.eksternBehandlingId,
                                                                     behandlingId = tekniskOpphør.tilkjentYtelseMedMetaData.behandlingId)
    }

    companion object {
        const val TYPE = "sjekkStatusPåOppdrag"
    }
}