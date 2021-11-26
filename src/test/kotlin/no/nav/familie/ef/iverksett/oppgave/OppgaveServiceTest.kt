package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class OppgaveServiceTest {

    val iverksettRepository = mockk<IverksettingRepository>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val oppgaveClient = mockk<OppgaveClient>()
    val oppfølgingsoppgaveBeskrivelse = mockk<OppfølgingsoppgaveBeskrivelse>()
    val oppgaveUtil = mockk<OppgaveUtil>()
    val oppgaveService = OppgaveService(oppgaveClient, familieIntegrasjonerClient, iverksettRepository)

    @Test
    fun `skalopprette for innvilget førstegangsbehandling, forvent true`() {
        val iverksett = mockk<Iverksett>()
        setupIverksettMock(
            iverksettMock = iverksett,
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksperioder = listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    fun `skalopprette for revurdering opphørt, forvent true`() {
        val iverksett = mockk<Iverksett>()
        every { iverksett.behandling.behandlingType } returns BehandlingType.REVURDERING
        every { iverksett.vedtak.vedtaksresultat } returns Vedtaksresultat.OPPHØRT
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    fun `skal opprette for revurdering avslått og ingen endring, forvent false`() {
        val iverksett = mockk<Iverksett>()
        setupIverksettMock(
            iverksett,
            BehandlingType.REVURDERING,
            Vedtaksresultat.AVSLÅTT,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        every { iverksettRepository.hent(any()) } returns iverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isFalse()
    }

    @Test
    fun `skalOpprette for revurdering INNVILGET med kun aktivitetsendring, forvent true`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
            iverksett,
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
            forrigeBehandlingIverksett,
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT))
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    fun `skalOpprette for revurdering INNVILGET med aktivitetsendring og periodeendring, forvent false`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
            iverksett,
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
            forrigeBehandlingIverksett,
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(
                vedtaksPeriode(
                    aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                    fraOgMed = LocalDate.now().plusMonths(2),
                    tilOgMed = LocalDate.now().plusMonths(3)
                )
            )
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isFalse()
    }

    private fun setupIverksettMock(
        iverksettMock: Iverksett,
        behandlingType: BehandlingType,
        vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
        vedtaksperioder: List<Vedtaksperiode>
    ) {
        every { iverksettMock.behandling.forrigeBehandlingId } returns UUID.randomUUID()
        every { iverksettMock.behandling.behandlingType } returns behandlingType
        every { iverksettMock.vedtak.vedtaksresultat } returns vedtaksresultat
        every { iverksettMock.vedtak.vedtaksperioder } returns vedtaksperioder
    }

    private fun vedtaksPeriode(
        aktivitet: AktivitetType,
        fraOgMed: LocalDate = LocalDate.now(),
        tilOgMed: LocalDate = LocalDate.now()
    ): Vedtaksperiode {
        return Vedtaksperiode(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            aktivitet = aktivitet,
            periodeType = VedtaksperiodeType.HOVEDPERIODE
        )
    }
}