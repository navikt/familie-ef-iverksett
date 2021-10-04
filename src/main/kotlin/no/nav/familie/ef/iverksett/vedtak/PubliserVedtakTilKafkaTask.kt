package no.nav.familie.ef.iverksett.vedtak

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.kontrakter.felles.ef.EnsligForsørgerVedtakhendelse
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = PubliserVedtakTilKafkaTask.TYPE,
                     beskrivelse = "Publiserer vedtak på kafka.",
                     settTilManuellOppfølgning = true)
class PubliserVedtakTilKafkaTask(private val taskRepository: TaskRepository,
                                 private val iverksettingRepository: IverksettingRepository,
                                 private val vedtakKafkaProducer: VedtakKafkaProducer) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        vedtakKafkaProducer.sendVedtak(EnsligForsørgerVedtakhendelse(
                fagsakId = iverksett.fagsak.eksternId,
                behandlingId = iverksett.behandling.eksternId,
                personIdent = iverksett.søker.personIdent,
                vedtaksdato = iverksett.vedtak.vedtaksdato,
                stønadType = iverksett.fagsak.stønadstype
        ))
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "publiserVedtakPåKafka"
    }
}