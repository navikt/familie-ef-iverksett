package no.nav.familie.ef.iverksett.startIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class IverksettService(val taskRepository: TaskRepository, val lagreIverksettService: LagreIverksettService) {

    fun startIverksetting(iverksett: Iverksett, brev: Brev) {

        lagreIverksettService.lagreIverksett(
            UUID.fromString(iverksett.behandlingId),
            iverksett,
            brev
        )
        val task = IverksettMotOppdragTask.opprettTask(
            iverksett.behandlingId,
            iverksett.personIdent,
            iverksett.tilkjentYtelse.saksbehandlerId
        )
        taskRepository.save(task)
    }


}