package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.util.behandlingsdetaljer
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.util.vedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class OppgaveServiceTest {

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
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent skalOpprette true`() {
        val iverksett = lagIverksett(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = emptyList()
        )
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    internal fun `revurdering opphørt, forvent skalOpprette true`() {
        val iverksett = lagIverksett(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = emptyList()
        )

        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    internal fun `revurdering avslått, forvent skalOpprette false`() {
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.AVSLÅTT,
            emptyList()
        )
        every { iverksettRepository.hent(any()) } returns iverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse
    }

    @Test
    internal fun `revurdering innvilget med kun aktivitetsendring, forvent skalOpprette true`() {
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        val forrigeBehandlingIverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT))
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, forrige behandling opphørt og uten perioder, forvent skalOpprette true`() {
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        val forrigeBehandlingIverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.OPPHØRT,
            listOf()
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent skalOpprette true`() {
        val iverksett = lagIverksett(
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
        val iverksett = lagIverksett(
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
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        val forrigeBehandlingIverksett = lagIverksett(
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
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        val forrigeBehandlingIverksett = lagIverksett(
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
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        )
        val forrigeBehandlingIverksett = lagIverksett(
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
        val iverksett = lagIverksett(
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
        val iverksett = lagIverksett(
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
        val iverksett = lagIverksett(
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
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.OPPHØRT,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            andelsdatoer = listOf(LocalDate.now())
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(any()) }
    }

    @Test
    internal fun `revurdering opphør, forvent at andel med maks tom dato blir sendt som arg til beskrivelse`() {
        val opphørsdato = slot<LocalDate>()
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.OPPHØRT,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            andelsdatoer = listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().minusMonths(1))
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(capture(opphørsdato)) }
        assertThat(opphørsdato.captured).isEqualTo(LocalDate.now())
    }

    @Test
    internal fun `av migreringssak, revurdering opphør, forvent at skalOppretteVurderHendelseOppgave er lik true`() {
        val forrigeBehandlingId = UUID.randomUUID()
        val vedtaksperioder = listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        val iverksett = lagIverksett(
            forrigeBehandlingId,
            BehandlingType.REVURDERING,
            Vedtaksresultat.OPPHØRT,
            vedtaksperioder
        )
        val forrigeIverksett = lagIverksett(
            null,
            BehandlingType.FØRSTEGANGSBEHANDLING,
            Vedtaksresultat.INNVILGET,
            vedtaksperioder,
            erMigrering = true
        )
        every { iverksettRepository.hent(forrigeBehandlingId) } returns forrigeIverksett

        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `av migreringssak, revurdering innvilget med aktivitetsendring, forvent at skalOppretteVurderHendelseOppgave er lik false`() {
        val iverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            andelsdatoer = listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().minusMonths(1))
        )
        val forrigeBehandlingIverksett = lagIverksett(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(
                vedtaksPeriode(
                    aktivitet = AktivitetType.UTVIDELSE_FORSØRGER_I_UTDANNING,
                    fraOgMed = LocalDate.now().plusMonths(3)
                )
            ),
            erMigrering = true
        )
        every { iverksettRepository.hent(any()) } returns forrigeBehandlingIverksett
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    private fun lagIverksett(
        forrigeBehandlingId: UUID? = null,
        behandlingType: BehandlingType,
        vedtaksresultat: Vedtaksresultat,
        vedtaksperioder: List<VedtaksperiodeOvergangsstønad>,
        erMigrering: Boolean = false,
        andelsdatoer: List<LocalDate> = emptyList()
    ): IverksettOvergangsstønad {
        val behandlingÅrsak = if (erMigrering) BehandlingÅrsak.MIGRERING else BehandlingÅrsak.SØKNAD
        return opprettIverksettOvergangsstønad(
            behandlingsdetaljer = behandlingsdetaljer(
                forrigeBehandlingId = forrigeBehandlingId,
                behandlingType = behandlingType,
                behandlingÅrsak = behandlingÅrsak
            ),
            vedtaksdetaljer = vedtaksdetaljerOvergangsstønad(
                vedtaksresultat = vedtaksresultat,
                vedtaksperioder = vedtaksperioder,
                andeler = andelsdatoer.map {
                    lagAndelTilkjentYtelse(beløp = 0, fraOgMed = it.minusDays(1), tilOgMed = it)
                },
                startdato = andelsdatoer.minByOrNull { it } ?: LocalDate.now()
            )
        )
    }

    private fun vedtaksPeriode(
        aktivitet: AktivitetType,
        fraOgMed: LocalDate = LocalDate.now(),
        tilOgMed: LocalDate = LocalDate.now()
    ): VedtaksperiodeOvergangsstønad {
        return VedtaksperiodeOvergangsstønad(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            aktivitet = aktivitet,
            periodeType = VedtaksperiodeType.HOVEDPERIODE
        )
    }
}
