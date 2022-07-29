package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = VedtakstatistikkTask.TYPE,
    beskrivelse = "Sender vedtaksstatistikk til DVH.",
    settTilManuellOppfølgning = true
)
class VedtakstatistikkTask(
    private val iverksettingRepository: IverksettingRepository,
    private val vedtakstatistikkService: VedtakstatistikkService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val forrigeIverksett = iverksett.behandling.forrigeBehandlingId?.let { iverksettingRepository.findByIdOrThrow(it) }?.data
        vedtakstatistikkService.sendTilKafka(iverksett, forrigeIverksett)
    }

    companion object {

        const val TYPE = "sendVedtakstatistikk"
    }
}
