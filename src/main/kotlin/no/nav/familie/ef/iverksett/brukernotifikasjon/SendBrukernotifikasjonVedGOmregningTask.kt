package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNestePubliseringTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
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
    val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data

        // Dobbeltsjekk: Tasken skal egentlig ikke være lagd hvis det ikke er G-omregning
        if (iverksett is IverksettOvergangsstønad && iverksett.erGOmregning()) {
            val generertMelding = brukernotifikasjonKafkaProducer.lagMelding(iverksett)

            brukernotifikasjonKafkaProducer.sendBeskjedTilBruker(
                personIdent = iverksett.søker.personIdent,
                iverksettOvergangsstønad = iverksett,
                behandlingId = behandlingId,
                melding = generertMelding,
            )
        }
    }

    override fun onCompletion(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        if (iverksett.erGOmregning()) {
            taskService.save(task.opprettNestePubliseringTask())
        }
    }

    companion object {
        const val TYPE = "sendBrukernotifikasjonVedGOmregningTask"
    }
}
