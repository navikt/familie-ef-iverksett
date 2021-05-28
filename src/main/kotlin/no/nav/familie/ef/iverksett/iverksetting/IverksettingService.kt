package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
class IverksettingService(val taskRepository: TaskRepository, val iverksettingRepository: IverksettingRepository,
                          val tilstandRepository: TilstandRepository) {

    fun startIverksetting(iverksett: Iverksett, brev: Brev) {

        iverksettingRepository.lagre(
                iverksett.behandling.behandlingId,
                iverksett,
                brev
        )

        tilstandRepository.opprettTomtResultat(iverksett.behandling.behandlingId)

        val førsteHovedflytTask = Task(type = IverksettMotOppdragTask.TYPE,
                                       payload = iverksett.behandling.behandlingId.toString(),
                                       properties = Properties().apply {
                                           this["personIdent"] = iverksett.søker.personIdent
                                           this["behandlingId"] = iverksett.behandling.behandlingId.toString()
                                           this["saksbehandler"] = iverksett.vedtak.saksbehandlerId
                                       })

        taskRepository.save(førsteHovedflytTask)
    }

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = tilstandRepository.hentIverksettResultat(behandlingId)
        iverksettResultat?.let {
            it.vedtaksbrevResultat?.let {
                return IverksettStatus.OK
            }
            it.journalpostResultat?.let {
                return IverksettStatus.JOURNALFØRT
            }
            it.oppdragResultat?.let {
                if (it.oppdragStatus == OppdragStatus.KVITTERT_OK) {
                    return IverksettStatus.OK_MOT_OPPDRAG
                }
                if (it.oppdragStatus == OppdragStatus.LAGT_PÅ_KØ) {
                    return IverksettStatus.SENDT_TIL_OPPDRAG
                }
                return IverksettStatus.FEILET_MOT_OPPDRAG
            }
            it.tilkjentYtelseForUtbetaling?.let {
                return IverksettStatus.SENDT_TIL_OPPDRAG
            }
            return IverksettStatus.IKKE_PÅBEGYNT
        } ?: return null
    }



}