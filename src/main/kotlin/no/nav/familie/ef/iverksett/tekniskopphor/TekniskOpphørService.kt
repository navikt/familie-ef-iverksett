package no.nav.familie.ef.iverksett.tekniskopphor

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TekniskOpphør
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TekniskOpphørService(val iverksettingRepository: IverksettingRepository,
                           val tilstandRepository: TilstandRepository,
                           val taskRepository: TaskRepository) {

    @Transactional
    fun startIverksettingAvTekniskOpphor(tekniskOpphør: TekniskOpphør) {
        val behandlingId = tekniskOpphør.tilkjentYtelseMedMetaData.behandlingId
        iverksettingRepository.lagreTekniskOpphør(behandlingId = behandlingId, tekniskOpphør)
        tilstandRepository.opprettTomtResultat(behandlingId)

        val førsteHovedflytTask = Task(type = IverksettTekniskOpphørTask.TYPE,
                                       payload = behandlingId.toString(),
                                       properties = Properties().apply {
                                           this["personIdent"] = tekniskOpphør.tilkjentYtelseMedMetaData.personIdent
                                           this["behandlingId"] = tekniskOpphør.tilkjentYtelseMedMetaData.behandlingId.toString()
                                           this["saksbehandler"] = tekniskOpphør.tilkjentYtelseMedMetaData.saksbehandlerId
                                       })

        taskRepository.save(førsteHovedflytTask)
    }
}