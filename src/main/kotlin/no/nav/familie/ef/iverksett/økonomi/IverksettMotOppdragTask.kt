package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.*


@Service
@TaskStepBeskrivelse(
    taskStepType = IverksettMotOppdragTask.TYPE,
    beskrivelse = "Utfører iverksetting av utbetalning mot økonomi."
)
class IverksettMotOppdragTask(val hentIverksettService: HentIverksettService, val oppdragClient: OppdragClient) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = task.payload
        var iverksett = hentIverksettService.hentIverksett(behandlingId)
        val utbetaling = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
            iverksett.tilkjentYtelse,
            iverksett.forrigeTilkjentYtelse
        )

        // Lagre denne (utbetaling, som en json)
        // Sende til oppdragssystemet
        utbetaling.utbetalingsoppdrag?.let { oppdragClient.iverksettOppdrag(it) }
            ?: error("Utbetalingsoppdrag mangler for iverksetting")
        // Lag ny task

    }


    companion object {

        fun opprettTask(behandlingId: String, personIdent: String, saksbehandler: String): Task {
            return Task(type = TYPE,
                        payload = behandlingId,
                        properties = Properties().apply {
                            this["personIdent"] = personIdent
                            this["behandlingId"] = behandlingId
                            this["saksbehandler"] = saksbehandler
                        })

        }

        const val TYPE = "utførIverksettingAvUtbetalning"
    }


}