package no.nav.familie.ef.iverksett.iverksett.hentstatus

import no.nav.familie.ef.iverksett.iverksett.domene.IverksettStatus
import no.nav.familie.ef.iverksett.iverksett.tilstand.hent.HentTilstandService
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HentIverksettStatusService(val hentTilstandService: HentTilstandService) {

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = hentTilstandService.hentIverksettResultat(behandlingId)
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