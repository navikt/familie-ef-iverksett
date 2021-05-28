package no.nav.familie.ef.iverksett.iverksettingstatus

import no.nav.familie.ef.iverksett.iverksettingstatus.status.tilstand.TilstandDbUtil
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class IverksettStatusService(val tilstandDbUtil: TilstandDbUtil) {

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = tilstandDbUtil.hentIverksettResultat(behandlingId)
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