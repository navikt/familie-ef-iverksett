package no.nav.familie.ef.iverksett.iverksetting.tilstand

import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IverksettResultatService(private val iverksettResultatRepository: IverksettResultatRepository) {

    fun opprettTomtResultat(behandlingId: UUID) {
        iverksettResultatRepository.insert(IverksettResultat(behandlingId))
    }

    fun oppdaterTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)
        iverksettResultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)
        iverksettResultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterJournalpostResultat(behandlingId: UUID, mottakerIdent: String, journalPostResultat: JournalpostResultat) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)

        val oppdatert = iverksettResultat.copy(
            journalpostResultat = iverksettResultat.journalpostResultat + mapOf(mottakerIdent to journalPostResultat)
        )
        iverksettResultatRepository.update(oppdatert)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterDistribuerVedtaksbrevResultat(
        behandlingId: UUID,
        journalpostId: String,
        distribuerVedtaksbrevResultat: DistribuerBrevResultat
    ) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)
        val oppdatert = iverksettResultat.copy(
            vedtaksbrevResultat = iverksettResultat.vedtaksbrevResultat + mapOf(journalpostId to distribuerVedtaksbrevResultat)
        )
        iverksettResultatRepository.update(oppdatert)
    }

    fun hentdistribuerVedtaksbrevResultat(behandlingId: UUID): Map<String, DistribuerBrevResultat>? {
        return iverksettResultatRepository.findByIdOrThrow(behandlingId).vedtaksbrevResultat.map
    }

    fun oppdaterTilbakekrevingResultat(behandlingId: UUID, tilbakekrevingResultat: TilbakekrevingResultat) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)
        iverksettResultatRepository.update(iverksettResultat.copy(tilbakekrevingResultat = tilbakekrevingResultat))
    }

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return iverksettResultatRepository.findByIdOrNull(behandlingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentTilkjentYtelse(behandlingId: Set<UUID>): Map<UUID, TilkjentYtelse> {
        val iverksettResultater = iverksettResultatRepository.findAllById(behandlingId)
        val tilkjenteYtelser = iverksettResultater.filter { it.tilkjentYtelseForUtbetaling != null }
            .associate { it.behandlingId to it.tilkjentYtelseForUtbetaling!! }
        if (behandlingId.size > tilkjenteYtelser.size) {
            error("Finner ikke tilkjent ytelse til behandlingIder=${behandlingId.minus(tilkjenteYtelser.keys)}}")
        }
        return tilkjenteYtelser
    }

    fun hentJournalpostResultat(behandlingId: UUID): Map<String, JournalpostResultat>? {
        return iverksettResultatRepository.findByIdOrNull(behandlingId)?.journalpostResultat?.map
    }

    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat? {
        return iverksettResultatRepository.findByIdOrNull(behandlingId)
    }

    fun hentTilbakekrevingResultat(behandlingId: UUID): TilbakekrevingResultat? {
        return iverksettResultatRepository.findByIdOrThrow(behandlingId).tilbakekrevingResultat
    }
}
