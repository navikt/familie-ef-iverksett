package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.kontrakter.ef.infotrygd.OpprettVedtakHendelseDto
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
@TaskStepBeskrivelse(
        taskStepType = SendFattetVedtakTilInfotrygdTask.TYPE,
        beskrivelse = "Sender hendelse om fattet vedtak til infotrygd"
)
class SendFattetVedtakTilInfotrygdTask(private val infotrygdFeedClient: InfotrygdFeedClient,
                                       private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
                                       private val iverksettingRepository: IverksettingRepository,
                                       private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)

        val stønadstype = iverksett.fagsak.stønadstype
        val personIdenter = familieIntegrasjonerClient.hentIdenter(iverksett.søker.personIdent, true)
                .map { it.personIdent }.toSet()
        val startDato = iverksett.vedtak.tilkjentYtelse.andelerTilkjentYtelse.minOfOrNull { it.periodebeløp.fraOgMed }
                        ?: error("Finner ikke noen andel med fraOgMed for behandling=$behandlingId")

        infotrygdFeedClient.opprettVedtakHendelse(OpprettVedtakHendelseDto(personIdenter, stønadstype, startDato))
    }

    override fun onCompletion(task: Task) {
        //taskRepository.save(task.opprettNesteTask()) // TODO når SendPerioderTilInfotrygdTask kommer
    }

    companion object {

        const val TYPE = "sendFattetVedtakTilInfotrygd"
    }
}
