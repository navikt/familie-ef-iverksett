package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseRevurderingInnvilget
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseRevurderingOpphørt
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OppgaveService(
        private val oppgaveClient: OppgaveClient,
        private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
        private val iverksettingRepository: IverksettingRepository
) {

    fun skalOppretteVurderHenvendelseOppgave(iverksett: Iverksett): Boolean {
        return when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> true
            BehandlingType.REVURDERING -> {
                when (iverksett.vedtak.vedtaksresultat) {
                    Vedtaksresultat.INNVILGET -> aktivitetEllerPeriodeEndret(iverksett)
                    Vedtaksresultat.OPPHØRT -> true
                    else -> false
                }
            }
            else -> false
        }
    }

    fun opprettVurderHenvendelseOppgave(iverksett: Iverksett): Long {
        val enhet = familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(iverksett.søker.personIdent)?.let { it }
                    ?: error("Kunne ikke finne enhetsnummer for personident med behandlingsId=${iverksett.behandling.behandlingId}")
        val beskrivelse = when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett)
            BehandlingType.REVURDERING -> finnBeskrivelseForRevurderingAvVedtaksresultat(iverksett)
            else -> error("Kunne ikke finne riktig BehandlingType for oppfølgingsoppgave")
        }
        val opprettOppgaveRequest =
                OppgaveUtil.opprettOppgaveRequest(iverksett.fagsak.eksternId,
                                                  iverksett.søker.personIdent,
                                                  iverksett.fagsak.stønadstype,
                                                  enhet,
                                                  Oppgavetype.VurderHenvendelse,
                                                  beskrivelse)
        return oppgaveClient.opprettOppgave(opprettOppgaveRequest)?.let { return it }
               ?: error("Kunne ikke finne oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }

    private fun finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett: Iverksett): String {
        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> beskrivelseFørstegangsbehandlingInnvilget(
                    iverksett.totalVedtaksperiode(),
                    iverksett.gjeldendeVedtak()
            )
            Vedtaksresultat.AVSLÅTT -> beskrivelseFørstegangsbehandlingAvslått(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
            else -> error("Kunne ikke finne riktig vedtaksresultat for oppfølgingsoppgave")
        }
    }

    private fun finnBeskrivelseForRevurderingAvVedtaksresultat(iverksett: Iverksett): String {
        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> {
                iverksett.behandling.forrigeBehandlingId?.let {
                    beskrivelseRevurderingInnvilget(
                            iverksett.totalVedtaksperiode(),
                            iverksett.gjeldendeVedtak())
                } ?: finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett)
            }
            Vedtaksresultat.OPPHØRT -> beskrivelseRevurderingOpphørt(opphørstato(iverksett))
            else -> error("Kunne ikke finne riktig vedtaksresultat for oppfølgingsoppgave")
        }
    }

    private fun opphørstato(iverksett: Iverksett): LocalDate? {
        val tilkjentYtelse = iverksett.vedtak.tilkjentYtelse ?: error("TilkjentYtelse er null")
        return tilkjentYtelse.andelerTilkjentYtelse.maxOfOrNull { it.tilOgMed }
    }

    private fun aktivitetEllerPeriodeEndret(iverksett: Iverksett): Boolean {
        val forrigeBehandlingId = iverksett.behandling.forrigeBehandlingId ?: return true
        val forrigeBehandling = iverksettingRepository.hent(forrigeBehandlingId)
        return harEndretAktivitet(iverksett, forrigeBehandling) || harEndretPeriode(iverksett, forrigeBehandling)
    }

    private fun harEndretAktivitet(iverksett: Iverksett, forrigeBehandling: Iverksett): Boolean {
        return iverksett.gjeldendeVedtak().aktivitet != forrigeBehandling.gjeldendeVedtak().aktivitet
    }

    private fun harEndretPeriode(iverksett: Iverksett, forrigeBehandling: Iverksett): Boolean {
        return iverksett.vedtaksPeriodeMedMaksTilOgMedDato() != forrigeBehandling.vedtaksPeriodeMedMaksTilOgMedDato()
    }

    private fun Iverksett.gjeldendeVedtak() = this.vedtak.vedtaksperioder.maxByOrNull { it.fraOgMed }?.let { it }
                                              ?: error("Kunne ikke finne vedtaksperioder")

    private fun Iverksett.vedtaksPeriodeMedMaksTilOgMedDato(): LocalDate {
        return this.vedtak.vedtaksperioder.maxOf { it.tilOgMed }
    }

    private fun Iverksett.totalVedtaksperiode(): Pair<LocalDate, LocalDate> =
            Pair(this.vedtak.vedtaksperioder.minOf { it.fraOgMed },
                 this.vedtak.vedtaksperioder.maxOf { it.tilOgMed })
}
