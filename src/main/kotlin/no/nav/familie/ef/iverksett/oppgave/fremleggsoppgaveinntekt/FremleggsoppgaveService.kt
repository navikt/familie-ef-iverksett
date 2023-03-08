package no.nav.familie.ef.iverksett.oppgave.fremleggsoppgaveinntekt

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.oppgave.OppgaveClient
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype

class FremleggsoppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
) {

    fun skalOppretteFremleggsoppgave(iverksett: IverksettOvergangsstønad): Boolean {
        return when {
            iverksett.vedtak.vedtaksresultat != Vedtaksresultat.INNVILGET -> false
            iverksett.behandling.behandlingType != BehandlingType.FØRSTEGANGSBEHANDLING -> false
            iverksett.vedtak.vedtaksperioder.none {
                it.periode.tomDato >= iverksett.vedtak.vedtakstidspunkt.toLocalDate().plusYears(1)
            } -> false
            else -> true
        }
    }

    fun opprettFremleggsoppgave(iverksett: IverksettOvergangsstønad): Long {
        val enhet = familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(iverksett.søker.personIdent)
            ?: error("Kunne ikke finne enhetsnummer for personident med behandlingsId=${iverksett.behandling.behandlingId}")
        val opprettOppgaveRequest =
            OppgaveUtil.opprettOppgaveRequest(
                eksternFagsakId = iverksett.fagsak.eksternId,
                personIdent = iverksett.søker.personIdent,
                stønadstype = iverksett.fagsak.stønadstype,
                enhetsnummer = enhet,
                oppgavetype = Oppgavetype.Fremlegg,
                beskrivelse = "Inntekt",
                settBehandlesAvApplikasjon = false,
                fristFerdigstillelse = iverksett.vedtak.vedtakstidspunkt.toLocalDate().plusYears(1),
            )
        return oppgaveClient.opprettOppgave(opprettOppgaveRequest)?.let { return it }
            ?: error("Kunne ikke opprette oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }
}
