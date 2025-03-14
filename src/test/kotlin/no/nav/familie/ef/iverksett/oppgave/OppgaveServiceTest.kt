package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.felles.util.DatoUtil
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.lagIverksettData
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForOpprettelseType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.Månedsperiode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

internal class OppgaveServiceTest {
    val iverksettRepository = mockk<IverksettingRepository>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val oppgaveClient = mockk<OppgaveClient>()
    val opprettOppgaveRequestSlot = slot<OpprettOppgaveRequest>()
    val oppgaveService = OppgaveService(oppgaveClient, familieIntegrasjonerClient, iverksettRepository)

    @BeforeEach
    internal fun init() {
        mockkObject(OppgaveUtil)
        mockkObject(OppgaveBeskrivelse)
        mockkObject(DatoUtil)
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns mockk()
        every { oppgaveClient.opprettOppgave(capture(opprettOppgaveRequestSlot)) } returns 0L
        every { OppgaveUtil.opprettOppgaveRequest(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns mockk()
        every { oppgaveClient.finnMapper(any(), any()) } returns FinnMappeResponseDto(0, emptyList())
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns Enhet("1234", "enhet")
        every { oppgaveClient.finnMapper(any(), any()) } returns FinnMappeResponseDto(0, emptyList())
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent skalOpprette true`() {
        val iverksett =
            lagIverksettData(
                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                vedtaksresultat = Vedtaksresultat.INNVILGET,
                vedtaksperioder = emptyList(),
            )
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    internal fun `revurdering opphørt, forvent skalOpprette true`() {
        val iverksett =
            lagIverksettData(
                behandlingType = BehandlingType.REVURDERING,
                vedtaksresultat = Vedtaksresultat.OPPHØRT,
                vedtaksperioder = emptyList(),
            )

        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    internal fun `revurdering avslått, forvent skalOpprette false`() {
        val iverksettData =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.AVSLÅTT,
                emptyList(),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(iverksettData)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksettData)).isFalse
    }

    @Test
    internal fun `revurdering innvilget med kun aktivitetsendring, forvent skalOpprette true`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        val forrigeBehandlingIverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT)),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, forrige er opphørt skal opprette opppgave`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        val forrigeBehandlingIverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                emptyList(),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, forrige behandling opphørt og uten perioder, forvent skalOpprette true`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        val forrigeBehandlingIverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent skalOpprette true`() {
        val iverksettData =
            lagIverksettData(
                null,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT)),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(iverksettData)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksettData)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {
        val iverksett =
            lagIverksettData(
                null,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT)),
            )

        oppgaveService.opprettOppgaveMedOppfølgingsenhet(
            iverksett,
            Oppgavetype.VurderHenvendelse,
            oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett),
        )
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any(), any()) }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any(), any()) }
    }

    @Test
    internal fun `revurdering innvilget med aktivitetsendring og periodeendring, forvent skalOpprette true`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        val forrigeBehandlingIverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(
                    vedtaksPeriode(
                        aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                        fraOgMed = LocalDate.now().plusMonths(2),
                        tilOgMed = LocalDate.now().plusMonths(3),
                    ),
                ),
            )
        val forrigeBehandlingId = iverksett.behandling.forrigeBehandlingId!!
        every { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) } returns
            lagIverksett(
                forrigeBehandlingIverksett,
            )
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()

        verify(exactly = 1) { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) }
    }

    @Test
    internal fun `revurdering innvilget med kun periodeendring, forvent skalOpprette true`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        val forrigeBehandlingIverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(
                    vedtaksPeriode(
                        aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                        fraOgMed = LocalDate.now().plusMonths(2),
                        tilOgMed = LocalDate.now().plusMonths(3),
                    ),
                ),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget med kun endring i fom dato, forvent skalOpprette false`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )
        val forrigeBehandlingIverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(
                    vedtaksPeriode(
                        aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                        fraOgMed = LocalDate.now().minusMonths(3),
                    ),
                ),
            )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )

        oppgaveService.opprettOppgaveMedOppfølgingsenhet(
            iverksett,
            Oppgavetype.VurderHenvendelse,
            oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett),
        )
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any(), any()) }
    }

    @Test
    internal fun `avslått førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingAvslått`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.AVSLÅTT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )

        oppgaveService.opprettOppgaveMedOppfølgingsenhet(
            iverksett,
            Oppgavetype.VurderHenvendelse,
            oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett),
        )
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått(any()) }
    }

    @Test
    internal fun `innvilget revurdering, forvent kall til beskrivelseRevurderingInnvilget`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
            )

        oppgaveService.opprettOppgaveMedOppfølgingsenhet(
            iverksett,
            Oppgavetype.VurderHenvendelse,
            oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett),
        )
        verify { OppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any(), any()) }
    }

    @Test
    internal fun `Hvis iverksetting av sanksjon, lag sanksjonsbeskrivelse på oppgave`() {
        val februar23 = YearMonth.of(2023, 2)
        val iverksett = lagIverksettOvergangsstønadSanksjon(februar23)

        val oppgavebeskrivelse = oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett)

        assertThat(oppgavebeskrivelse).isEqualTo("Bruker har fått vedtak om sanksjon 1 mnd: februar 2023")
    }

    @Test
    internal fun `opphørt revurdering, forvent kall til beskrivelseRevurderingOpphørt`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
                andelsdatoer = listOf(YearMonth.now()),
            )

        oppgaveService.opprettOppgaveMedOppfølgingsenhet(
            iverksett,
            Oppgavetype.VurderHenvendelse,
            oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett),
        )
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(any()) }
    }

    @Test
    internal fun `revurdering opphør, forvent at andel med maks tom dato blir sendt som arg til beskrivelse`() {
        val opphørsdato = slot<LocalDate>()
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
                andelsdatoer = listOf(YearMonth.now().minusMonths(2), YearMonth.now(), YearMonth.now().minusMonths(1)),
            )

        oppgaveService.opprettOppgaveMedOppfølgingsenhet(
            iverksett,
            Oppgavetype.VurderHenvendelse,
            oppgaveService.lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett),
        )
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(capture(opphørsdato)) }
        assertThat(opphørsdato.captured).isEqualTo(YearMonth.now().atEndOfMonth())
    }

    @Test
    internal fun `av migreringssak, revurdering opphør, forvent at skalOppretteVurderHendelseOppgave er lik true`() {
        val forrigeBehandlingId = UUID.randomUUID()
        val vedtaksperioder = listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        val iverksett =
            lagIverksettData(
                forrigeBehandlingId,
                BehandlingType.REVURDERING,
                Vedtaksresultat.OPPHØRT,
                vedtaksperioder,
            )
        val forrigeIverksett =
            lagIverksettData(
                null,
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.INNVILGET,
                vedtaksperioder,
                erMigrering = true,
            )
        every { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) } returns lagIverksett(forrigeIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering, forrige behandling er gOmregnet, periodetype migrering, forvent skalOppretteVurderHendelseOppgave lik false`() {
        val forrigeBehandlingId = UUID.randomUUID()
        val vedtaksperioder = listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        val forrigeVedtaksperioder =
            listOf(
                vedtaksPeriode(
                    fraOgMed = LocalDate.of(LocalDate.now().year, 5, 1),
                    tilOgMed = LocalDate.of(LocalDate.now().year.plus(2), 10, 31),
                    aktivitet = AktivitetType.MIGRERING,
                    periodeType = VedtaksperiodeType.MIGRERING,
                ),
            )
        val iverksett =
            lagIverksettData(
                forrigeBehandlingId,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                vedtaksperioder = vedtaksperioder,
            )
        val forrigeIverksett =
            lagIverksettData(
                null,
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.INNVILGET,
                vedtaksperioder = forrigeVedtaksperioder,
                årsak = BehandlingÅrsak.G_OMREGNING,
            )
        every { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) } returns lagIverksett(forrigeIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `revurdering, forrige behandling er gOmregnet, periodetype ikke migrering, forvent skalOppretteVurderHendelseOppgave lik true`() {
        val forrigeBehandlingId = UUID.randomUUID()
        val vedtaksperioder = listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID))
        val forrigeVedtaksperioder =
            listOf(
                vedtaksPeriode(
                    fraOgMed = LocalDate.of(LocalDate.now().year, 5, 1),
                    tilOgMed = LocalDate.of(LocalDate.now().year.plus(2), 10, 31),
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            )
        val iverksett =
            lagIverksettData(
                forrigeBehandlingId,
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                vedtaksperioder = vedtaksperioder,
            )
        val forrigeIverksett =
            lagIverksettData(
                null,
                BehandlingType.FØRSTEGANGSBEHANDLING,
                Vedtaksresultat.INNVILGET,
                vedtaksperioder = forrigeVedtaksperioder,
            )
        every { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) } returns lagIverksett(forrigeIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `av migreringssak, revurdering innvilget, forvent at skalOppretteVurderHendelseOppgave er lik false`() {
        val iverksett =
            lagIverksettData(
                UUID.randomUUID(),
                BehandlingType.REVURDERING,
                Vedtaksresultat.INNVILGET,
                listOf(vedtaksPeriode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID)),
                andelsdatoer = listOf(YearMonth.now().minusMonths(2), YearMonth.now(), YearMonth.now().minusMonths(1)),
            )
        val forrigeBehandlingIverksett = lagMigreringsIverksetting()
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `Forrige behandling er migreringssak, iverksettbehandling er av type sanksjon - skal opprette vurder hendelse oppgave`() {
        val iverksett = lagIverksettOvergangsstønadSanksjon()
        val forrigeBehandlingIverksett = lagMigreringsIverksetting()

        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)

        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    fun `Skal kunne hente oppgave på oppgaveId`() {
        every { oppgaveClient.finnOppgaveMedId(any()) } returns lagEksternTestOppgave()
        val oppgave = oppgaveService.hentOppgave(GSAK_OPPGAVE_ID)

        assertThat(oppgave.id).isEqualTo(GSAK_OPPGAVE_ID)
    }

    @Test
    fun `Frist for kontroll av selvstendig skal skje den femtende desember angitt år`() {
        val årstallEttÅrFremITid = LocalDate.now().plusYears(1).year
        val årstallToÅrFremITid = LocalDate.now().plusYears(2).year

        val iverksett =
            lagIverksettData(
                behandlingType = BehandlingType.REVURDERING,
                vedtaksresultat = Vedtaksresultat.INNVILGET,
                vedtaksperioder = emptyList(),
                oppgavetyper = listOf(OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE),
                årForInntektskontrollSelvstendigNæringsdrivende = årstallEttÅrFremITid,
            )

        val fristFerdigstillelse = oppgaveService.lagFristFerdigstillelse(iverksett)

        assertThat(fristFerdigstillelse).isEqualTo(LocalDate.of(årstallEttÅrFremITid, 12, 15))
        assertThat(fristFerdigstillelse).isNotEqualTo(LocalDate.of(årstallToÅrFremITid, 12, 15))
    }

    @Test
    fun `Frist for kontroll av inntektskontroll ett år frem i tid skal skje en dag senere når det faller på den sjette i en måned`() {
        val vedtaksdato = LocalDate.of(2022, 12, 6)
        val nyForventetFrist = LocalDate.of(2023, 12, 7)

        val fristFerdigstillelse = oppgaveService.lagFristFerdigstillelseForInntektskontrollEttÅrFrem(vedtaksdato)

        assertThat(nyForventetFrist).isEqualTo(fristFerdigstillelse)
    }

    @Test
    fun `Frist for kontroll av inntektskontroll ett år frem i tid skal endre seg når det faller på en helg`() {
        val vedtaksdato = LocalDate.of(2022, 12, 3)
        val nyForventetFrist = LocalDate.of(2023, 12, 1)

        val fristFerdigstillelse = oppgaveService.lagFristFerdigstillelseForInntektskontrollEttÅrFrem(vedtaksdato)

        assertThat(nyForventetFrist).isEqualTo(fristFerdigstillelse)
    }

    @Test
    internal fun `mapOppgaveForOpprettelseTypeTilMappeId skal returnere mappe 41 for INNTEKTSKONTROLL_1_ÅR_FREM_I_TID`() {
        val result = oppgaveService.mapOppgaveForOpprettelseTypeTilMappeNavn(OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID)
        assertThat(result).isEqualTo(Enhetsmappe.REVURDERING)
    }

    @Test
    internal fun `mapOppgaveForOpprettelseTypeTilMappeId skal returnere mappe 61 for INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE`() {
        val result = oppgaveService.mapOppgaveForOpprettelseTypeTilMappeNavn(OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE)
        assertThat(result).isEqualTo(Enhetsmappe.SELVSTENDIG_NÆRINGSDRIVENDE)
    }

    private fun lagMigreringsIverksetting() =
        lagIverksettData(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(
                vedtaksPeriode(
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                    fraOgMed = LocalDate.now().minusMonths(3),
                    periodeType = VedtaksperiodeType.MIGRERING,
                ),
            ),
        )

    private fun lagIverksettOvergangsstønadSanksjon(sanksjonsmåned: YearMonth = YearMonth.now()): IverksettOvergangsstønad {
        val månedsperiode = Månedsperiode(fom = sanksjonsmåned, tom = sanksjonsmåned)
        val vedtaksPeriode =
            VedtaksperiodeOvergangsstønad(
                periode = månedsperiode,
                periodeType = VedtaksperiodeType.SANKSJON,
                aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
            )
        val andeler = listOf(sanksjonsmåned.minusMonths(1), sanksjonsmåned.plusMonths(1))
        return lagIverksettData(
            UUID.randomUUID(),
            BehandlingType.REVURDERING,
            Vedtaksresultat.INNVILGET,
            listOf(vedtaksPeriode),
            andelsdatoer = andeler,
            årsak = BehandlingÅrsak.SANKSJON_1_MND,
        )
    }

    private fun vedtaksPeriode(
        aktivitet: AktivitetType,
        fraOgMed: LocalDate = LocalDate.now(),
        tilOgMed: LocalDate = LocalDate.now(),
        periodeType: VedtaksperiodeType = VedtaksperiodeType.HOVEDPERIODE,
    ): VedtaksperiodeOvergangsstønad =
        VedtaksperiodeOvergangsstønad(
            periode = Månedsperiode(fraOgMed, tilOgMed),
            aktivitet = aktivitet,
            periodeType = periodeType,
        )

    private fun lagEksternTestOppgave(): no.nav.familie.kontrakter.felles.oppgave.Oppgave =
        no.nav.familie.kontrakter.felles.oppgave
            .Oppgave(id = GSAK_OPPGAVE_ID)

    companion object {
        private const val GSAK_OPPGAVE_ID = 12345L
    }
}
