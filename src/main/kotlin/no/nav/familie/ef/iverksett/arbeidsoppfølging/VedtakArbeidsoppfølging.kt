package no.nav.familie.ef.iverksett.arbeidsoppfølging

import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import java.util.UUID

/**
 * TODO : Hvilke felter skal bli med her ?
 */
data class VedtakArbeidsoppfølging(
    val behandlingId: UUID,
    val vedtaksperioder: List<Vedtaksperiode>,
    val beskrivelse : String
)