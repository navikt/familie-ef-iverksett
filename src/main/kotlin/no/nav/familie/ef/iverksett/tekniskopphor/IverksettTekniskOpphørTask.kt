package no.nav.familie.ef.iverksett.tekniskopphor

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
        taskStepType = IverksettTekniskOpphørTask.TYPE,
        beskrivelse = "Utfører iverksetting av teknisk opphør mot økonomi."
)
class IverksettTekniskOpphørTask(val iverksettingRepository: IverksettingRepository,
                                 val oppdragClient: OppdragClient,
                                 val taskRepository: TaskRepository,
                                 val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val tekniskOpphør = iverksettingRepository.hentTekniskOpphør(behandlingId)
        val forrigeTilkjentYtelse = tekniskOpphør.forrigeBehandlingId.let {
            tilstandRepository.hentTilkjentYtelse(it) ?: error("Kunne ikke finne tilkjent ytelse for behandlingId=${it}")
        }
        val utbetaling =
                UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(tekniskOpphør.tilkjentYtelseMedMetaData,
                                                                                   forrigeTilkjentYtelse)

        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId = behandlingId, utbetaling)
        utbetaling.utbetalingsoppdrag?.let { oppdragClient.iverksettOppdrag(it) }
        ?: error("Utbetalingsoppdrag mangler for iverksetting")
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task(type = VentePåStatusFørTekniskOpphørTask.TYPE, payload = task.payload))
    }

    companion object {
        const val TYPE = "utførIverksettingAvTekniskOpphør"
    }
}