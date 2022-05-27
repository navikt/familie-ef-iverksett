package no.nav.familie.ef.iverksett.tekniskopphor

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
    taskStepType = VentePåStatusFørTekniskOpphørTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 5 * 60L,
    beskrivelse = "Sjekker status på teknisk opphør mot økonomi."
)

class VentePåStatusFørTekniskOpphørTask(
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService,
    private val taskRepository: TaskRepository,
    private val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val tekniskOpphør = iverksettingRepository.hentTekniskOpphør(behandlingId)
        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(
            tekniskOpphør.tilkjentYtelseMedMetaData.stønadstype,
            tekniskOpphør.tilkjentYtelseMedMetaData.personIdent,
            tekniskOpphør.tilkjentYtelseMedMetaData.eksternBehandlingId,
            tekniskOpphør.tilkjentYtelseMedMetaData.behandlingId
        )
    }

    companion object {

        const val TYPE = "sjekkStatusTekniskOpphør"
    }
}
