package no.nav.familie.ef.iverksett.iverksett.start

import no.nav.familie.ef.iverksett.iverksett.Brev
import no.nav.familie.ef.iverksett.iverksett.Iverksett
import no.nav.familie.ef.iverksett.iverksett.lagre.LagreIverksettService
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandService
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.Properties

@Service
class IverksettService(val taskRepository: TaskRepository, val lagreIverksettService: LagreIverksettService,
                       val lagreTilstandService: LagreTilstandService) {

    fun startIverksetting(iverksett: Iverksett, brev: Brev) {

        lagreIverksettService.lagreIverksett(
                iverksett.behandling.behandlingId,
                iverksett,
                brev
        )

        lagreTilstandService.opprettTomtResultat(iverksett.behandling.behandlingId)

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