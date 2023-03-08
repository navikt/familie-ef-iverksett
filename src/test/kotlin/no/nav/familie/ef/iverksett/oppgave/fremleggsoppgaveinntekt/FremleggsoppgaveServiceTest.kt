package no.nav.familie.ef.iverksett.oppgave.fremleggsoppgaveinntekt

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.lagIverksettData
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse
import no.nav.familie.ef.iverksett.oppgave.OppgaveClient
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.Månedsperiode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class FremleggsoppgaveServiceTest {

    val familieIntegrasjonerClient: FamilieIntegrasjonerClient = mockk()
    val oppgaveClient: OppgaveClient = mockk()
    val fremleggsoppgaveService = FremleggsoppgaveService(oppgaveClient, familieIntegrasjonerClient)

    @BeforeEach
    internal fun init() {
        mockkObject(OppgaveUtil)
        mockkObject(OppgaveBeskrivelse)
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns mockk()
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppgaveUtil.opprettOppgaveRequest(any(), any(), any(), any(), any(), any(), any()) } returns mockk()
    }

    @Test
    internal fun `skal ikke opprette fremleggsoppgave når en førstegangsbehandling er avslått`() {
        val iverksett = lagIverksettData(
            forrigeBehandlingId = UUID.randomUUID(),
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
            vedtaksperioder = listOf(
                vedtaksPeriode(
                    tilOgMed = LocalDate.now().plusMonths(13),
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID
                )
            ),
            vedtakstidspunkt = LocalDateTime.now()
        )
        assertThat(fremleggsoppgaveService.skalOppretteFremleggsoppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `skal ikke opprette en fremleggsoppgave ved en revurdering`() {
        val iverksett = lagIverksettData(
            forrigeBehandlingId = UUID.randomUUID(),
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(
                vedtaksPeriode(
                    tilOgMed = LocalDate.now().plusMonths(13),
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID
                )
            ),
            vedtakstidspunkt = LocalDateTime.now()
        )
        assertThat(fremleggsoppgaveService.skalOppretteFremleggsoppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `skal ikke opprette fremleggsoppgave hvis den seneste vedtaksperioden ender før det har gått ett år`() {
        val iverksett = lagIverksettData(
            forrigeBehandlingId = UUID.randomUUID(),
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(
                vedtaksPeriode(tilOgMed = LocalDate.now().plusMonths(11), aktivitet = AktivitetType.FORSØRGER_I_ARBEID),
                vedtaksPeriode(tilOgMed = LocalDate.now().plusMonths(10), aktivitet = AktivitetType.FORSØRGER_I_ARBEID)
            ),
            vedtakstidspunkt = LocalDateTime.now()
        )
        assertThat(fremleggsoppgaveService.skalOppretteFremleggsoppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `skal opprette fremleggsoppgave hvis den seneste vedtaksperioden er over ett år frem i tid`() {
        val iverksett = lagIverksettData(
            forrigeBehandlingId = UUID.randomUUID(),
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(
                vedtaksPeriode(tilOgMed = LocalDate.now().plusMonths(13), aktivitet = AktivitetType.FORSØRGER_I_ARBEID),
                vedtaksPeriode(tilOgMed = LocalDate.now().plusMonths(9), aktivitet = AktivitetType.FORSØRGER_I_ARBEID)
            ),
            vedtakstidspunkt = LocalDateTime.now()
        )
        assertThat(fremleggsoppgaveService.skalOppretteFremleggsoppgave(iverksett)).isTrue()
    }

    private fun vedtaksPeriode(
        aktivitet: AktivitetType,
        fraOgMed: LocalDate = LocalDate.now(),
        tilOgMed: LocalDate = LocalDate.now(),
        periodeType: VedtaksperiodeType = VedtaksperiodeType.HOVEDPERIODE,
    ): VedtaksperiodeOvergangsstønad {
        return VedtaksperiodeOvergangsstønad(
            periode = Månedsperiode(fraOgMed, tilOgMed),
            aktivitet = aktivitet,
            periodeType = periodeType,
        )
    }
}