package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.kontrakter.ef.infotrygd.OpprettPeriodeHendelseDto
import no.nav.familie.kontrakter.ef.infotrygd.Periode
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID


@Service
@TaskStepBeskrivelse(
        taskStepType = SendPerioderTilInfotrygdTask.TYPE,
        beskrivelse = "Sender periodehendelse til infotrygd"
)
class SendPerioderTilInfotrygdTask(private val infotrygdFeedClient: InfotrygdFeedClient,
                                   private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
                                   private val iverksettingRepository: IverksettingRepository,
                                   private val taskRepository: TaskRepository,
                                   @Value("\${NAIS_CLUSTER_NAME:prod}") private val cluster: String) : AsyncTaskStep {

    override fun doTask(task: Task) {
        if (!cluster.contains("dev")) {
            error("Må håndtere fullOvergangsstønad før denne kjøres i prod")
        }
        val iverksett = iverksettingRepository.hent(UUID.fromString(task.payload))
        val stønadstype = iverksett.fagsak.stønadstype
        val personIdenter = familieIntegrasjonerClient.hentIdenter(iverksett.søker.personIdent, true)
                .map { it.personIdent }.toSet()
        val perioder = iverksett.vedtak.tilkjentYtelse.andelerTilkjentYtelse.map {
            Periode(startdato = it.periodebeløp.fraOgMed,
                    sluttdato = it.periodebeløp.tilOgMed,
                    fullOvergangsstønad = true) // TODO må settes ut fra hvor mye som er redusert/max
        }

        infotrygdFeedClient.opprettPeriodeHendelse(OpprettPeriodeHendelseDto(personIdenter, stønadstype, perioder))
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "sendPerioderTilInfotrygd"
    }
}
