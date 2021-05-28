package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksettingstatus.status.tilstand.TilstandDbUtil
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.Properties

@Service
class IverksettService(val taskRepository: TaskRepository, val iverksettDbUtil: IverksettDbUtil,
                       val tilstandDbUtil: TilstandDbUtil) {

    fun startIverksetting(iverksett: Iverksett, brev: Brev) {

        iverksettDbUtil.lagreIverksett(
                iverksett.behandling.behandlingId,
                iverksett,
                brev
        )

        tilstandDbUtil.opprettTomtResultat(iverksett.behandling.behandlingId)

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