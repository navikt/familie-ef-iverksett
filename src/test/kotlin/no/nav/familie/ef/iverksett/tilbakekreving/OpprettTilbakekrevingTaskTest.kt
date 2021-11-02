package no.nav.familie.ef.iverksett.tilbakekreving

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.util.opprettTilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OpprettTilbakekrevingTaskTest {

    private val tilstandRepository = mockk<TilstandRepository>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val tilbakekrevingClient = mockk<TilbakekrevingClient>()
    private val simuleringService = mockk<SimuleringService>()
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()

    private val opprettTilbakekrevingTask = OpprettTilbakekrevingTask(
            tilstandRepository = tilstandRepository,
            iverksettingRepository = iverksettingRepository,
            tilbakekrevingClient = tilbakekrevingClient,
            simuleringService = simuleringService,
            familieIntegrasjonerClient = familieIntegrasjonerClient
    )

    @BeforeEach
    protected fun init() {
        every { tilbakekrevingClient.finnesÅpenBehandling(any()) } returns false
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns
                Enhet("1", "Oslo")
        every { tilbakekrevingClient.opprettBehandling(any()) } returns ""
    }

    @Test
    protected fun `uendret og ingen feilutbetaling`() {

        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = null)
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(0)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every { tilstandRepository.oppdaterTilbakekrevingResultat(behandlingsId, any()) } just Runs

        doTask(behandlingsId)

        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    protected fun `uendret, postiv feilutbetaling`() {

        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = tilbakekreving)
        val tilbakekrevingResultatSlot = slot<TilbakekrevingResultat>()
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(100)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every { tilstandRepository.oppdaterTilbakekrevingResultat(behandlingsId, capture(tilbakekrevingResultatSlot)) } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { tilbakekrevingClient.opprettBehandling(any()) }

        val request = tilbakekrevingResultatSlot.captured.opprettTilbakekrevingRequest
        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling).isEqualTo(tilbakekreving.tilbakekrevingMedVarsel?.sumFeilutbetaling)
    }

    @Test
    protected fun `oppdager feilutbetaling på iverksett`() {

        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = null)
        val tilbakekrevingResultatSlot = slot<TilbakekrevingResultat>()
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(100)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every { tilstandRepository.oppdaterTilbakekrevingResultat(behandlingsId, capture(tilbakekrevingResultatSlot)) } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { tilbakekrevingClient.opprettBehandling(any()) }

        val request = tilbakekrevingResultatSlot.captured.opprettTilbakekrevingRequest
        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL)
        assertThat(request.varsel).isNull()
    }

    @Test
    protected fun `feilutbetaling forsvinner på iverksett`() {

        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = tilbakekreving)
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(0)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat

        doTask(behandlingsId)

        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    protected fun `feilutbetaling endres`() {

        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = tilbakekreving)
        val tilbakekrevingResultatSlot = slot<TilbakekrevingResultat>()
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(200)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every { tilstandRepository.oppdaterTilbakekrevingResultat(behandlingsId, capture(tilbakekrevingResultatSlot)) } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { tilbakekrevingClient.opprettBehandling(any()) }

        val request = tilbakekrevingResultatSlot.captured.opprettTilbakekrevingRequest
        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling).isEqualTo(beriketSimuleringsresultat.oppsummering.feilutbetaling)
    }

    private fun doTask(behandlingsId: UUID) {
        val task = Task(OpprettTilbakekrevingTask.TYPE, payload = behandlingsId.toString())
        opprettTilbakekrevingTask.doTask(task)
    }

    fun Tilbakekrevingsdetaljer.medFeilutbetaling(beløp: Long) =
            this.copy(tilbakekrevingMedVarsel = this.tilbakekrevingMedVarsel?.copy(sumFeilutbetaling = beløp.toBigDecimal()))

    fun BeriketSimuleringsresultat.medFeilutbetaling(beløp: Long) =
            this.copy(oppsummering = this.oppsummering.copy(feilutbetaling = beløp.toBigDecimal()))
}