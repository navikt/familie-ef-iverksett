package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
@TaskStepBeskrivelse(
        taskStepType = IverksettMotOppdragTask.TYPE,
        beskrivelse = "Utfører iverksetting av utbetalning mot økonomi."
)
class IverksettMotOppdragTask(val iverksettingRepository: IverksettingRepository,
                              val oppdragClient: OppdragClient,
                              val taskRepository: TaskRepository,
                              val tilstandRepository: TilstandRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        val forrigeTilkjentYtelse = iverksett.behandling.forrigeBehandlingId?.let {
            tilstandRepository.hentTilkjentYtelse(it) ?: error("Kunne ikke finne tilkjent ytelse for behandlingId=${it}")
        }
        val nyTilkjentYtelseMedMetaData =
                iverksett.vedtak.tilkjentYtelse?.toMedMetadata(saksbehandlerId = iverksett.vedtak.saksbehandlerId,
                                                               eksternBehandlingId = iverksett.behandling.eksternId,
                                                               stønadType = iverksett.fagsak.stønadstype,
                                                               eksternFagsakId = iverksett.fagsak.eksternId,
                                                               personIdent = iverksett.søker.personIdent,
                                                               behandlingId = iverksett.behandling.behandlingId,
                                                               vedtaksdato = iverksett.vedtak.vedtaksdato)
                ?: error("Mangler tilkjent ytelse på vedtaket")

        val utbetaling = lagTilkjentYtelseMedUtbetalingsoppdrag(nyTilkjentYtelseMedMetaData,
                                                                forrigeTilkjentYtelse)

        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId = behandlingId, utbetaling)
        utbetaling.utbetalingsoppdrag?.let { oppdragClient.iverksettOppdrag(it) }
        ?: error("Utbetalingsoppdrag mangler for iverksetting")
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "utførIverksettingAvUtbetaling"
    }
}