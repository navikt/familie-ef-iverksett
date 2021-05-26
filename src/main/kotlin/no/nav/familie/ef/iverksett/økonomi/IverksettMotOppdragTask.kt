package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksett.toMedMetadata
import no.nav.familie.ef.iverksett.iverksett.hent.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.tilstand.hent.HentTilstandService
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
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
class IverksettMotOppdragTask(val hentIverksettService: HentIverksettService,
                              val oppdragClient: OppdragClient,
                              val taskRepository: TaskRepository,
                              val lagreTilstandService: LagreTilstandService,
                              val hentTilstandService: HentTilstandService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = hentIverksettService.hentIverksett(behandlingId)
        val forrigeTilkjentYtelse = iverksett.behandling.forrigeBehandlingId?.let {
            hentTilstandService.hentTilkjentYtelse(it) ?: error("Kunne ikke finne tilkjent ytelse for behandlingId=${it}")
        }
        val utbetaling = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                iverksett.vedtak.tilkjentYtelse.toMedMetadata(saksbehandlerId = iverksett.vedtak.saksbehandlerId,
                                                              eksternBehandlingId = iverksett.behandling.eksternId,
                                                              stønadType = iverksett.fagsak.stønadstype,
                                                              eksternFagsakId = iverksett.fagsak.eksternId,
                                                              personIdent = iverksett.søker.personIdent,
                                                              behandlingId = iverksett.behandling.behandlingId,
                                                              vedtaksdato = iverksett.vedtak.vedtaksdato

                ),
                forrigeTilkjentYtelse
        )

        lagreTilstandService.lagreTilkjentYtelseForUtbetaling(behandlingId = behandlingId, utbetaling)
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