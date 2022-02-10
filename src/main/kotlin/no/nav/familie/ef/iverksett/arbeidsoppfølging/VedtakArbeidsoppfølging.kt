package no.nav.familie.ef.iverksett.arbeidsoppfølging

import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype

/**
 * TODO : Hvilke felter skal bli med her ?
 */
data class VedtakArbeidsoppfølging(
        val eksternFagsakId: Long,
        val personIdent: String,
        val stønadstype: StønadType,
        val enhetsnummer: Enhet,
        val oppgavetype: Oppgavetype,
        val vedtaksperioder: List<Vedtaksperiode>,
        val beskrivelse: String
)