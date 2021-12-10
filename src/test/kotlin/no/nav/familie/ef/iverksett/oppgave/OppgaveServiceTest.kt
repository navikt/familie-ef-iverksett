package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class OppgaveServiceTest {

    val iverksettRepository = mockk<IverksettingRepository>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val oppgaveClient = mockk<OppgaveClient>()
    val oppgaveService = OppgaveService(oppgaveClient, familieIntegrasjonerClient, iverksettRepository)

    @BeforeEach
    internal fun init() {
        mockkObject(OppgaveUtil)
        mockkObject(OppfølgingsoppgaveBeskrivelse)
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent skalOpprette true`() {
        val iverksett = mockk<Iverksett>()
        setupIverksettMock(iverksett,
                           UUID.randomUUID(),
                           BehandlingType.FØRSTEGANGSBEHANDLING,
                           Vedtaksresultat.INNVILGET,
                           emptyList())
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering opphørt, forvent skalOpprette true`() {
        val iverksett = mockk<Iverksett>()
        setupIverksettMock(iverksett, UUID.randomUUID(), BehandlingType.REVURDERING, Vedtaksresultat.OPPHØRT, emptyList())
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering avslått, forvent skalOpprette false`() {
        val iverksett = mockk<Iverksett>()
        setupIverksettMock(iverksett, UUID.randomUUID(), BehandlingType.REVURDERING, Vedtaksresultat.AVSLÅTT, emptyList())
        every { iverksettRepository.hent(any()) } returns iverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `revurdering innvilget med kun aktivitetsendring, forvent skalOpprette true`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
                forrigeBehandlingIverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT))
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }
    @Test
    internal fun `revurdering innvilget med kun aktivitetsendring, men avslått f-behandling, forvent skalOpprette true`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
                iverksett,
                null,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
                forrigeBehandlingIverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT))
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }
    @Test
    internal fun `revurdering innvilget med aktivitetsendring og periodeendring, forvent skalOpprette true`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
                forrigeBehandlingIverksett,
                UUID.randomUUID(),
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
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget med kun periodeendring, forvent skalOpprette true`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
                forrigeBehandlingIverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(
                        vedtaksPeriode(
                                aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                                fraOgMed = LocalDate.now().plusMonths(2),
                                tilOgMed = LocalDate.now().plusMonths(3)
                        )
                )
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget med kun endring i fom dato, forvent skalOpprette false`() {
        val iverksett = mockk<Iverksett>()
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupIverksettMock(
                forrigeBehandlingIverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(
                        vedtaksPeriode(
                                aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                                fraOgMed = LocalDate.now().plusMonths(3)
                        )
                )
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {
        val iverksett = mockk<Iverksett>()
        every { OppgaveUtil.opprettVurderHenvendelseOppgaveRequest(any(), any(), any()) } returns mockk()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns Enhet("id", "navn")
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppfølgingsoppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any(), any()) } returns ""

        oppgaveService.opprettVurderHendelseOppgave(iverksett)
        verify { OppfølgingsoppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any(), any()) }
    }

    @Test
    internal fun `avslått førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingAvslått`() {
        val iverksett = mockk<Iverksett>()
        every { OppgaveUtil.opprettVurderHenvendelseOppgaveRequest(any(), any(), any()) } returns mockk()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.AVSLÅTT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns Enhet("id", "navn")
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppfølgingsoppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått(any()) } returns ""

        oppgaveService.opprettVurderHendelseOppgave(iverksett)
        verify { OppfølgingsoppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått(any()) }
    }

    @Test
    internal fun `innvilget revurdering, forvent kall til beskrivelseRevurderingInnvilget`() {
        val iverksett = mockk<Iverksett>()
        every { OppgaveUtil.opprettVurderHenvendelseOppgaveRequest(any(), any(), any()) } returns mockk()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns Enhet("id", "navn")
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppfølgingsoppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any(), any()) } returns ""

        oppgaveService.opprettVurderHendelseOppgave(iverksett)
        verify { OppfølgingsoppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any(), any()) }
    }

    @Test
    internal fun `opphørt revurdering, forvent kall til beskrivelseRevurderingOpphørt`() {
        val iverksett = mockk<Iverksett>()
        every { OppgaveUtil.opprettVurderHenvendelseOppgaveRequest(any(), any(), any()) } returns mockk()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns Enhet("id", "navn")
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppfølgingsoppgaveBeskrivelse.beskrivelseRevurderingOpphørt(any()) } returns ""

        oppgaveService.opprettVurderHendelseOppgave(iverksett)
        verify { OppfølgingsoppgaveBeskrivelse.beskrivelseRevurderingOpphørt(any()) }
    }

    private fun setupIverksettMock(
            iverksettMock: Iverksett,
            forrigeBehandlingId: UUID?,
            behandlingType: BehandlingType,
            vedtaksresultat: Vedtaksresultat,
            vedtaksperioder: List<Vedtaksperiode>
    ) {
        every { iverksettMock.behandling.forrigeBehandlingId } returns forrigeBehandlingId
        every { iverksettMock.behandling.behandlingId } returns UUID.randomUUID()
        every { iverksettMock.behandling.behandlingType } returns behandlingType
        every { iverksettMock.vedtak.vedtaksresultat } returns vedtaksresultat
        every { iverksettMock.vedtak.vedtaksperioder } returns vedtaksperioder
        every { iverksettMock.søker.personIdent } returns "12345678910"
        every { iverksettMock.vedtak.vedtakstidspunkt } returns LocalDateTime.MIN
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