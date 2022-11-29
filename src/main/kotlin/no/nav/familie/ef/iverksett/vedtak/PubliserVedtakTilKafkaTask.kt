package no.nav.familie.ef.iverksett.vedtak

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.felles.ef.EnsligForsørgerVedtakhendelse
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import java.util.UUID
import no.nav.familie.kontrakter.felles.ef.StønadType as EksternStønadType

@Service
@TaskStepBeskrivelse(
    taskStepType = PubliserVedtakTilKafkaTask.TYPE,
    beskrivelse = "Publiserer vedtak på kafka.",
    settTilManuellOppfølgning = true
)
class PubliserVedtakTilKafkaTask(
    private val taskService: TaskService,
    private val iverksettingRepository: IverksettingRepository,
    private val vedtakKafkaProducer: VedtakKafkaProducer
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        vedtakKafkaProducer.sendVedtak(
            EnsligForsørgerVedtakhendelse(
                behandlingId = iverksett.behandling.eksternId,
                personIdent = iverksett.søker.personIdent,
                stønadType = EksternStønadType.valueOf(iverksett.fagsak.stønadstype.name)
            )
        )
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "publiserVedtakPåKafka"
    }
}
