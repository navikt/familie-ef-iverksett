package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.infotrygd.OpprettVedtakHendelseDto
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendFattetVedtakTilInfotrygdTask.TYPE,
    beskrivelse = "Sender hendelse om fattet vedtak til infotrygd"
)
class SendFattetVedtakTilInfotrygdTask(
    private val infotrygdFeedClient: InfotrygdFeedClient,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val iverksettingRepository: IverksettingRepository,
    private val taskRepository: TaskRepository
) : AsyncTaskStep {

    private final val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksettData = iverksettingRepository.findByIdOrThrow(behandlingId).data
        if (iverksettData.behandling.forrigeBehandlingId != null) {
            logger.info(
                "Sender ikke ett nytt vedtak til infotrygd for fagsak=${iverksettData.fagsak.fagsakId}" +
                    " då det allerede er sendt på forrige behandling"
            )
            return
        }

        val stønadstype = iverksettData.fagsak.stønadstype
        val personIdenter = familieIntegrasjonerClient.hentIdenter(iverksettData.søker.personIdent, true)
            .map { it.personIdent }.toSet()
        val tilkjentYtelse: TilkjentYtelse =
            iverksettData.vedtak.tilkjentYtelse
                ?: error("Finner ikke tilkjent ytelse for behandling med id=${iverksettData.behandling.behandlingId}")
        val startDato = tilkjentYtelse.andelerTilkjentYtelse.minOfOrNull { it.fraOgMed }
            ?: error("Finner ikke noen andel med fraOgMed for behandling=$behandlingId")

        infotrygdFeedClient.opprettVedtakHendelse(OpprettVedtakHendelseDto(personIdenter, stønadstype, startDato))
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "sendFattetVedtakTilInfotrygd"
    }
}
