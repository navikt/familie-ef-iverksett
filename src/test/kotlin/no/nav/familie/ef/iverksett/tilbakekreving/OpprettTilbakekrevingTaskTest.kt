package no.nav.familie.ef.iverksett.tilbakekreving

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.util.opprettTilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.kontrakter.ef.iverksett.VergeDto
import no.nav.familie.kontrakter.ef.iverksett.Vergetype
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
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
    private val taskRepository = mockk<TaskRepository>()


    private val opprettTilbakekrevingTask = OpprettTilbakekrevingTask(
            tilstandRepository = tilstandRepository,
            taskRepository = taskRepository,
            iverksettingRepository = iverksettingRepository,
            tilbakekrevingClient = tilbakekrevingClient,
            simuleringService = simuleringService,
            familieIntegrasjonerClient = familieIntegrasjonerClient
    )

    @BeforeEach
    fun init() {
        every { tilbakekrevingClient.finnesÅpenBehandling(any()) } returns false
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(any()) } returns
                Enhet("1", "Oslo")
        every { tilbakekrevingClient.opprettBehandling(any()) } returns ""
    }

    @Test
    fun `uendret og ingen feilutbetaling`() {

        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = null, forrigeBehandlingId = UUID.randomUUID())
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(0)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every { tilstandRepository.oppdaterTilbakekrevingResultat(behandlingsId, any()) } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { simuleringService.hentBeriketSimulering(any()) }
        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `førstegangsbehandling skal ikke opprette tilbakekreving`() {
        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = null, forrigeBehandlingId = null)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett

        doTask(behandlingsId)

        verify(exactly = 0) { simuleringService.hentBeriketSimulering(any()) }
        verify(exactly = 0) { tilstandRepository.oppdaterTilbakekrevingResultat(any(), any()) }
        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `uendret, postiv feilutbetaling`() {

        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = tilbakekreving, forrigeBehandlingId = UUID.randomUUID())
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
    fun `oppdager feilutbetaling på iverksett`() {

        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = null, forrigeBehandlingId = UUID.randomUUID())
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
    fun `feilutbetaling forsvinner på iverksett`() {

        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = tilbakekreving, forrigeBehandlingId = UUID.randomUUID())
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(0)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksett
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat

        doTask(behandlingsId)

        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `feilutbetaling endres`() {

        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksett(behandlingsId, tilbakekreving = tilbakekreving, forrigeBehandlingId = UUID.randomUUID())
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

    @Test
    internal fun `skal opprette IverksettMotOppdragTask når OpprettTilbakekrevingTask er ferdig`() {
        val taskSlot = slot<Task>()
        val behandlingId = UUID.randomUUID().toString()
        val task = Task(OpprettTilbakekrevingTask.TYPE, payload = behandlingId)
        every { taskRepository.save(capture(taskSlot)) } returns task
        opprettTilbakekrevingTask.onCompletion(task)
        assertThat(taskSlot.captured.payload).isEqualTo(behandlingId)
        assertThat(taskSlot.captured.type).isEqualTo(IverksettMotOppdragTask.TYPE)
    }

    @Test
    internal fun `verge sendes med til tilbakekreving`() {
        val behandlingsId = UUID.randomUUID()
        val verge = VergeDto(ident = "11111111", navn = "Verge Vergesen", vergetype = Vergetype.VOKSEN)
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksettMedVergeOgTilbakekreving = opprettIverksett(behandlingsId,
                                                                 tilbakekreving = tilbakekreving,
                                                                 forrigeBehandlingId = UUID.randomUUID()).let {
            it.copy(vedtak = it.vedtak.copy(verge = verge.toDomain()))
        }
        val tilbakekrevingResultatSlot = slot<TilbakekrevingResultat>()
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(100)

        every { iverksettingRepository.hent(behandlingsId) } returns iverksettMedVergeOgTilbakekreving
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every { tilstandRepository.oppdaterTilbakekrevingResultat(behandlingsId, capture(tilbakekrevingResultatSlot)) } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { tilbakekrevingClient.opprettBehandling(any()) }

        val request = tilbakekrevingResultatSlot.captured.opprettTilbakekrevingRequest
        assertThat(request.verge).isNotNull
        assertThat(request.verge?.personIdent).isEqualTo(verge.ident)
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