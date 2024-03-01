package no.nav.familie.ef.iverksett.arena

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendFattetVedtakTilArenaTask.TYPE,
    beskrivelse = "Sender hendelse om fattet vedtak til arena",
)
class SendFattetVedtakTilArenaTask(
    private val vedtakhendelseProducer: VedtakhendelseProducer,
    private val integrasjonerClient: FamilieIntegrasjonerClient,
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)
        val aktørId = integrasjonerClient.hentAktørId(iverksett.data.søker.personIdent)
        vedtakhendelseProducer.produce(mapIverksettTilVedtakHendelser(iverksett.data, aktørId))
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {
        const val TYPE = "sendFattetVedtakTilArena"
    }
}
