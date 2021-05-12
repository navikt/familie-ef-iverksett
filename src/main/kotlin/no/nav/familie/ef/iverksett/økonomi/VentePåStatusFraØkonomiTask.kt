package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksettService
import no.nav.familie.ef.iverksett.journalføring.JournalførVedtaksbrevTask
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
@TaskStepBeskrivelse(
    taskStepType = VentePåStatusFraØkonomiTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Sjekker status på utbetalningsoppdraget mot økonomi."
)

class VentePåStatusFraØkonomiTask(val hentIverksettService: HentIverksettService, val oppdragClient: OppdragClient) :
    AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = hentIverksettService.hentIverksett(behandlingId.toString())
        val oppdragId = OppdragId(
            fagsystem = iverksett.tilkjentYtelse.stønadstype.tilKlassifisering(),
            personIdent = iverksett.personIdent,
            behandlingsId = iverksett.behandlingId
        )

        val oppdragstatus = oppdragClient.hentStatus(oppdragId)
        when (oppdragstatus) {
            OppdragStatus.KVITTERT_OK -> return
            else -> error("Status fra oppdrag er ikke ok, status : ${oppdragstatus}")
        }
    }

    override fun onCompletion(task: Task) {
       val nesteTask = JournalførVedtaksbrevTask.opprettTask(task.payload)
        lagJournalførTask()
    }

    fun lagJournalførTask() {
        // TODO : Implement later
    }

    companion object {

        fun opprettTask(behandlingId: String): Task =
            Task(
                type = TYPE,
                payload = behandlingId,
                triggerTid = LocalDateTime.now().plusMinutes(15)
            )


        const val TYPE = "sjekkStatusPåOppdrag"
    }


}