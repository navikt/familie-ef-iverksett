package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.kontrakter.ef.infotrygd.OpprettPeriodeHendelseDto
import no.nav.familie.kontrakter.ef.infotrygd.Periode
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.util.UUID


@Service
@TaskStepBeskrivelse(
        taskStepType = SendPerioderTilInfotrygdTask.TYPE,
        beskrivelse = "Sender periodehendelse til infotrygd"
)
class SendPerioderTilInfotrygdTask(private val infotrygdFeedClient: InfotrygdFeedClient,
                                   private val iverksettingRepository: IverksettingRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val iverksett = iverksettingRepository.hent(UUID.fromString(task.payload))
        val stønadstype = iverksett.fagsak.stønadstype
        val personIdenter = iverksett.søker.personIdenter
        val perioder = iverksett.vedtak.tilkjentYtelse.andelerTilkjentYtelse.map {
            Periode(startdato = it.periodebeløp.fraOgMed,
                    sluttdato = it.periodebeløp.tilOgMed,
                    fullOvergangsstønad = true) // TODO må settes ut fra hvor mye som er redusert/max
        }

        infotrygdFeedClient.opprettPeriodeHendelse(OpprettPeriodeHendelseDto(personIdenter, stønadstype, perioder))
    }

    companion object {

        const val TYPE = "sendPerioderTilInfotrygd"
    }
}
