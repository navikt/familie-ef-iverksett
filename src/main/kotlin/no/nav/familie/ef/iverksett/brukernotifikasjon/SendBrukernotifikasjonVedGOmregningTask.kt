package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendBrukernotifikasjonVedGOmregningTask.TYPE,
    beskrivelse = "Sender melding til bruker via dittnav om at vedtak er G-omregnet",
    settTilManuellOppfølgning = true,
)
class SendBrukernotifikasjonVedGOmregningTask(
    val brukernotifikasjonKafkaProducer: BrukernotifikasjonKafkaProducer,
    val iverksettingRepository: IverksettingRepository,
    val featureToggleService: FeatureToggleService,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        if (iverksett.erGOmregning() && featureToggleService.isEnabled("familie.ef.sak.g-beregning-scheduler")) { // Dobbeltsjekk: Tasken skal egentlig ikke være lagd hvis det ikke er G-omregning
            if (nyesteGrunnbeløp.periode.fomDato.plusYears(1) > DatoUtil.dagensDato()) {
                brukernotifikasjonKafkaProducer.sendBeskjedTilBruker(iverksett, behandlingId)
            } else {
                throw RuntimeException("Skal ikke sende melding til dittnav dersom grunnbeløpet ikke er oppdatert")
            }
        }
    }

    companion object {
        const val TYPE = "sendBrukernotifikasjonVedGOmregningTask"
    }
}

object DatoUtil { // For å kunne mocke dato
    fun dagensDato() = LocalDate.now()
}
