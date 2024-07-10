package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.infotrygd.OpprettPeriodeHendelseDto
import no.nav.familie.kontrakter.ef.infotrygd.Periode
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendPerioderTilInfotrygdTask.TYPE,
    beskrivelse = "Sender periodehendelse til infotrygd",
)
class SendPerioderTilInfotrygdTask(
    private val infotrygdFeedClient: InfotrygdFeedClient,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val iverksett = iverksettingRepository.findByIdOrThrow(UUID.fromString(task.payload)).data
        val stønadstype = iverksett.fagsak.stønadstype
        if (stønadstype != StønadType.OVERGANGSSTØNAD) {
            return
        }
        val personIdenter =
            familieIntegrasjonerClient
                .hentIdenter(iverksett.søker.personIdent, true)
                .map { it.personIdent }
                .toSet()
        val perioder =
            iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.map {
                Periode(
                    startdato = it.periode.fomDato,
                    sluttdato = it.periode.tomDato,
                    fullOvergangsstønad = it.erFullOvergangsstønad(),
                )
            } ?: error("Kan ikke finne tilkjentYtelse for behandling med id=${iverksett.behandling.behandlingId}")

        infotrygdFeedClient.opprettPeriodeHendelse(OpprettPeriodeHendelseDto(personIdenter, stønadstype, perioder))
    }

    override fun onCompletion(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        if (iverksett.erMigrering()) {
            logger.info(
                "Siste tasken i publiseringsflyt er SendPerioderTilInfotrygd før vedtakstatistikk sendes for behandling=$behandlingId då årsaken er migrering",
            )
        }
        taskService.save(task.opprettNestePubliseringTask(iverksett.erMigrering()))
    }

    companion object {
        const val TYPE = "sendPerioderTilInfotrygd"
    }
}
