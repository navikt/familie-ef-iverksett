package no.nav.familie.ef.iverksett.hentIverksettStatus

import no.nav.familie.ef.iverksett.domene.IverksettStatus
import no.nav.familie.ef.iverksett.tilstand.hent.HentTilstandService
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class HentIverksettStatusService(val hentTilstandService: HentTilstandService) {

    fun utledStatus(behandlingId: UUID): IverksettStatus {
        val iverksettResultat = hentTilstandService.hentIverksettResultat(behandlingId)
        iverksettResultat?.let {
            it.vedtaksbrevResultat?.let {
                return IverksettStatus.DISTRIBUERT
            }
            it.journalpostResultat?.let {
                return IverksettStatus.JOURNALFØRT
            }
            it.oppdragResultat?.let {
                if (it.oppdragStatus == OppdragStatus.KVITTERT_OK) {
                    return IverksettStatus.OK_MOT_OPPDRAG
                }
                return IverksettStatus.FEILET_MOT_OPPDRAG
            }
            it.tilkjentYtelseForUtbetaling?.let {
                return IverksettStatus.SENDT_TIL_OPPDRAG
            }
        }
    }
}