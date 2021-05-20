package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.domene.toMedMetadata
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*


@Service
@TaskStepBeskrivelse(
        taskStepType = IverksettMotOppdragTask.TYPE,
        beskrivelse = "Utfører iverksetting av utbetalning mot økonomi."
)
class IverksettMotOppdragTask(val hentIverksettService: HentIverksettService,
                              val oppdragClient: OppdragClient,
                              val taskRepository: TaskRepository,
                              val lagreTilstandService: LagreTilstandService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = hentIverksettService.hentIverksett(behandlingId)
        val forrigeTilkjentYtelse = null // TODO: Hent ut forrigeTilkjentYtelse fra lokal DB
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