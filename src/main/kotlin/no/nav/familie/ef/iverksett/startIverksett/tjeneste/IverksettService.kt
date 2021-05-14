package no.nav.familie.ef.iverksett.startIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class IverksettService(val taskRepository: TaskRepository, val lagreIverksettService: LagreIverksettService) {

    fun startIverksetting(iverksett: Iverksett, brev: Brev) {

        lagreIverksettService.lagreIverksett(
            iverksett.behandling.behandlingId,
            iverksett,
            brev
        )

        val førsteHovedflytTask = Task(type = IverksettMotOppdragTask.TYPE,
            payload = iverksett.behandling.behandlingId.toString(),
            properties = Properties().apply {
                this["personIdent"] = iverksett.søker.personIdent
                this["behandlingId"] = iverksett.behandling.behandlingId.toString()
                this["saksbehandler"] = iverksett.vedtak.saksbehandlerId
            })

        taskRepository.save(førsteHovedflytTask)
    }


}