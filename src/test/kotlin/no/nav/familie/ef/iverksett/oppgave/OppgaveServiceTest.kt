package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class OppgaveServiceTest {

    val iverksett = mockk<Iverksett>()
    val iverksettRepository = mockk<IverksettingRepository>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val oppgaveClient = mockk<OppgaveClient>()
    val oppgaveService = OppgaveService(oppgaveClient, familieIntegrasjonerClient, iverksettRepository)

    @BeforeEach
    internal fun init() {
        mockkObject(OppgaveUtil)
        mockkObject(OppgaveBeskrivelse)
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns mockk()
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppgaveUtil.opprettOppgaveRequest(any(), any(), any(), any(), any(), any()) } returns mockk()
        every { iverksett.erMigrering() } returns false
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent skalOpprette true`() {
        setupIverksettMock(iverksett,
                           UUID.randomUUID(),
                           BehandlingType.FØRSTEGANGSBEHANDLING,
                           Vedtaksresultat.INNVILGET,
                           emptyList())
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering opphørt, forvent skalOpprette true`() {
        setupIverksettMock(iverksett, UUID.randomUUID(), BehandlingType.REVURDERING, Vedtaksresultat.OPPHØRT, emptyList())
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering avslått, forvent skalOpprette false`() {
        setupIverksettMock(iverksett, UUID.randomUUID(), BehandlingType.REVURDERING, Vedtaksresultat.AVSLÅTT, emptyList())
        every { iverksettRepository.hent(any()) } returns iverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `revurdering innvilget med kun aktivitetsendring, forvent skalOpprette true`() {
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        every { forrigeBehandlingIverksett.erMigrering() } returns false
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
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent skalOpprette true`() {

        setupIverksettMock(
                iverksett,
                null,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT))
        )
        every { iverksettRepository.hent(any()) } returns iverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {

        setupIverksettMock(
                iverksett,
                null,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT))
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any(), any()) }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any(), any()) }
    }

    @Test
    internal fun `revurdering innvilget med aktivitetsendring og periodeendring, forvent skalOpprette true`() {
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        every { forrigeBehandlingIverksett.erMigrering() } returns false
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
        val forrigeBehandlingId = iverksett.behandling.forrigeBehandlingId!!
        every { iverksettRepository.hent(forrigeBehandlingId) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()

        verify(exactly = 1) { iverksettRepository.hent(forrigeBehandlingId) }
    }

    @Test
    internal fun `revurdering innvilget med kun periodeendring, forvent skalOpprette true`() {
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        every { forrigeBehandlingIverksett.erMigrering() } returns false
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
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget med kun endring i fom dato, forvent skalOpprette false`() {
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        every { forrigeBehandlingIverksett.erMigrering() } returns false
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
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any(), any()) }
    }

    @Test
    internal fun `avslått førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingAvslått`() {
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.AVSLÅTT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått(any()) }
    }

    @Test
    internal fun `innvilget revurdering, forvent kall til beskrivelseRevurderingInnvilget`() {
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any(), any()) }
    }

    @Test
    internal fun `opphørt revurdering, forvent kall til beskrivelseRevurderingOpphørt`() {
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupAndeler(iverksett, listOf(LocalDate.now()))

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(any()) }
    }

    @Test
    internal fun `revurdering opphør, forvent at andel med maks tom dato blir sendt som arg til beskrivelse`() {
        val opphørsdato = slot<LocalDate>()
        setupIverksettMock(
                iverksett,
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        setupAndeler(iverksett, listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().minusMonths(1)))

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(capture(opphørsdato)) }
        assertThat(opphørsdato.captured).isEqualTo(LocalDate.now())
    }

    @Test
    internal fun `av migreringssak, revurdering opphør, forvent at skalOppretteVurderHendelseOppgave er lik true`() {
        val forrigeBehandling = mockk<Iverksett>()
        every { iverksett.erMigrering() } returns false
        every { forrigeBehandling.erMigrering() } returns true
        setupIverksettMock(
            iverksett,
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.OPPHØRT,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `av migreringssak, revurdering innvilget med aktivitetsendring, forvent at skalOppretteVurderHendelseOppgave er lik false`() {
        val forrigeBehandlingIverksett = mockk<Iverksett>()
        every { iverksett.erMigrering() } returns false
        every { forrigeBehandlingIverksett.erMigrering() } returns true
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
                    aktivitet = AktivitetType.UTVIDELSE_FORSØRGER_I_UTDANNING,
                    fraOgMed = LocalDate.now().plusMonths(3)
                )
            )
        )
        setupAndeler(iverksett, listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().minusMonths(1)))
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHendelseOppgave(iverksett)).isFalse()
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
        every { iverksettMock.fagsak.eksternId } returns 0L
        every { iverksettMock.fagsak.stønadstype } returns StønadType.OVERGANGSSTØNAD
    }

    private fun setupAndeler(iverksettMock: Iverksett, tilOgMedDatoer: List<LocalDate>) {
        val andeler = tilOgMedDatoer.map { lagAndelTilkjentYtelse(beløp = 0, fraOgMed = it.minusDays(1), tilOgMed = it) }
        every { iverksettMock.vedtak.tilkjentYtelse!!.andelerTilkjentYtelse } returns andeler
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