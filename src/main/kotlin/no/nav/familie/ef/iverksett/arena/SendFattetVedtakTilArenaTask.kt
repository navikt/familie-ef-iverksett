package no.nav.familie.ef.iverksett.arena

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
@TaskStepBeskrivelse(
        taskStepType = SendFattetVedtakTilArenaTask.TYPE,
        beskrivelse = "Sender hendelse om fattet vedtak til arena"
)
class SendFattetVedtakTilArenaTask(private val vedtakhendelseProducer: VedtakhendelseProducer,
                                   private val integrasjonerClient: FamilieIntegrasjonerClient,
                                   private val iverksettingRepository: IverksettingRepository,
                                   private val taskRepository: TaskRepository
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        val aktørId = integrasjonerClient.hentAktørId(iverksett.søker.personIdent)
        vedtakhendelseProducer.produce(mapIverksettTilVedtakHendelser(iverksett, aktørId))
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "sendFattetVedtakTilArena"
    }
}
