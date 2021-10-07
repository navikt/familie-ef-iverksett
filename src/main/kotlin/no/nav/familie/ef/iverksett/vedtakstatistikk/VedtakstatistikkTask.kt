package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = VedtakstatistikkTask.TYPE,
                     beskrivelse = "Sender vedtaksstatistikk til DVH.",
                     settTilManuellOppfølgning = true)
class VedtakstatistikkTask(val iverksettingRepository: IverksettingRepository,
                           val vedtakstatistikkService: VedtakstatistikkService,
                           val tilstandRepository: TilstandRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingId)
        vedtakstatistikkService.sendTilKafka(iverksett, tilkjentYtelse)
    }

    companion object {

        const val TYPE = "sendVedtakstatistikk"
    }
}