package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import org.springframework.stereotype.Service

@Service
class OppgaveService(private val oppgaveClient: OppgaveClient,
                     private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
                     private val iverksettingRepository: IverksettingRepository) {

    fun skalOppretteVurderHendelsOppgave(iverksett: Iverksett): Boolean {
        return when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> true
            BehandlingType.TEKNISK_OPPHØR -> true
            BehandlingType.REVURDERING -> {
                if (iverksett.vedtak.vedtaksresultat == Vedtaksresultat.AVSLÅTT) {
                    aktivitetEndretPeriodeUendret(iverksett)
                }
                true
            }
            else -> false
        }
    }

    fun opprettVurderHendelseOppgave(iverksett: Iverksett) {
        val enhetsnummer = familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(iverksett.søker.personIdent)
        val beskrivelse = when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> {
                OppfølgingsoppgaveBeskrivelse.beskrivelseFørstegangsbehandling(iverksett)
            }
            BehandlingType.REVURDERING -> {
                OppfølgingsoppgaveBeskrivelse.beskrivelseRevurdering(iverksett)
            }
            else -> error("Kunne ikke finne riktig BehandlingType for oppfølgingsoppgave")
        }
        val opprettOppgaveRequest = OppgaveUtil.opprettOppgaveRequest(iverksett, enhetsnummer, beskrivelse)
        oppgaveClient.opprettOppgave(opprettOppgaveRequest)
    }

    private fun hentForrigeBehandling(iverksett: Iverksett): Iverksett {
        return iverksett.behandling.forrigeBehandlingId?.let {
            iverksettingRepository.hent(it)
        } ?: error("Mangler forrigeBehandlingId på revurdering for behandling=${iverksett.behandling.behandlingId}")
    }

    private fun aktivitetEndretPeriodeUendret(iverksett: Iverksett): Boolean {
        return aktivitetEndret(iverksett) && !perioderEndret(iverksett)
    }

    private fun aktivitetEndret(iverksett: Iverksett): Boolean {
        val forrigeBehandling = hentForrigeBehandling(iverksett)
        return iverksett.gjeldendeVedtak().aktivitet.equals(forrigeBehandling)
    }

    private fun perioderEndret(iverksett: Iverksett): Boolean {
        val forrigeBehandling = hentForrigeBehandling(iverksett)
        return iverksett.vedtaksPeriode().equals(forrigeBehandling.vedtaksPeriode())
    }
}
