package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
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
        triggerTidVedFeilISekunder = 15 * 60L,
        beskrivelse = "Sjekker status på utbetalningsoppdraget mot økonomi."
)

class VentePåStatusFraØkonomiTask(
        val iverksettingRepository: IverksettingRepository,
        val oppdragClient: OppdragClient,
        val taskRepository: TaskRepository,
        val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        val oppdragId = OppdragId(
                fagsystem = iverksett.fagsak.stønadstype.tilKlassifisering(),
                personIdent = iverksett.søker.personIdent,
                behandlingsId = iverksett.behandling.behandlingId.toString()
        )

        val oppdragstatus = oppdragClient.hentStatus(oppdragId)
        tilstandRepository.oppdaterOppdragResultat(
                behandlingId = behandlingId,
                OppdragResultat(oppdragStatus = oppdragstatus)
        )
        when (oppdragstatus) {
            OppdragStatus.KVITTERT_OK -> return
            else -> error("Status fra oppdrag er ikke ok, status : ${oppdragstatus}")
        }
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "sjekkStatusPåOppdrag"
    }
}