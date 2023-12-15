package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdragNy
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = IverksettMotOppdragTask.TYPE,
    beskrivelse = "Utfører iverksetting av utbetalning mot økonomi.",
)
class IverksettMotOppdragTask(
    private val iverksettingRepository: IverksettingRepository,
    private val oppdragClient: OppdragClient,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
) : AsyncTaskStep {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val forrigeTilkjentYtelse = iverksett.behandling.forrigeBehandlingId?.let {
            iverksettResultatService.hentTilkjentYtelse(it)
                ?: error("Kunne ikke finne tilkjent ytelse for behandlingId=$it")
        }
        val nyTilkjentYtelseMedMetaData =
            iverksett.vedtak.tilkjentYtelse?.toMedMetadata(
                saksbehandlerId = iverksett.vedtak.saksbehandlerId,
                eksternBehandlingId = iverksett.behandling.eksternId,
                stønadType = iverksett.fagsak.stønadstype,
                eksternFagsakId = iverksett.fagsak.eksternId,
                personIdent = iverksett.søker.personIdent,
                behandlingId = iverksett.behandling.behandlingId,
                vedtaksdato = iverksett.vedtak.vedtakstidspunkt.toLocalDate(),
            ) ?: error("Mangler tilkjent ytelse på vedtaket")

        log.info("Bruker ny utbetalingsgenerator for behandling=${iverksett.behandling.behandlingId}")
        val utbetaling =
            lagTilkjentYtelseMedUtbetalingsoppdragNy(
                nyTilkjentYtelseMedMetaData,
                forrigeTilkjentYtelse,
                iverksett.erGOmregning(),
            )

        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId = behandlingId, utbetaling)
        utbetaling.utbetalingsoppdrag?.let {
            if (it.utbetalingsperiode.isNotEmpty()) {
                oppdragClient.iverksettOppdrag(it)
            } else {
                log.warn("Iverksetter ikke noe mot oppdrag. Ingen utbetalingsperioder. behandlingId=$behandlingId")
            }
        }
            ?: error("Utbetalingsoppdrag mangler for iverksetting")
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "utførIverksettingAvUtbetaling"
    }
}
