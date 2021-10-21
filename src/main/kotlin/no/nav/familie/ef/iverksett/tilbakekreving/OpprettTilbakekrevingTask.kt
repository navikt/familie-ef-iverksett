package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.tilSimulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = OpprettTilbakekrevingTask.TYPE,
                     maxAntallFeil = 50,
                     settTilManuellOppfølgning = true,
                     triggerTidVedFeilISekunder = 15 * 60L,
                     beskrivelse = "Opprett tilbakekrevingsbehandling")
class OpprettTilbakekrevingTask(private val iverksettingRepository: IverksettingRepository,
                                private val tilbakekrevingClient: TilbakekrevingClient,
                                private val tilstandRepository: TilstandRepository,
                                private val simuleringService: SimuleringService,
                                private val familieIntegrasjonerClient: FamilieIntegrasjonerClient) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val originalIverksett = iverksettingRepository.hent(behandlingId)
        val beriketSimuleringsresultat = simuleringService.hentBeriketSimulering(originalIverksett.tilSimulering())
        val nyIverksett = originalIverksett.oppfriskTilbakekreving(beriketSimuleringsresultat)

        if (nyIverksett.skalTilbakekreves() && finnesÅpenTilbakekrevingsbehandling(nyIverksett)) {
            logger.info("Det finnnes allerede tilbakekrevingsbehandling for behandling=[${behandlingId}]")
        } else if (nyIverksett.skalTilbakekreves()) {
            // Denne burde komme fra ef-sak i DTO'en. Se Tea-6884
            val enhet = hentEnhet(nyIverksett)
            val opprettTilbakekrevingRequest = nyIverksett.tilOpprettTilbakekrevingRequest(enhet)

            tilbakekrevingClient.opprettBehandling(opprettTilbakekrevingRequest)
            tilstandRepository.oppdaterTilbakekrevingResultat(
                    behandlingId = behandlingId,
                    TilbakekrevingResultat(opprettTilbakekrevingRequest))

            logger.info("Opprettet tilbakekrevingsbehandling for behandling=[${behandlingId}]")
        } else {
            logger.debug("Tilbakekreving ikke funnet for behandling=[${behandlingId}]")
        }
    }

    private fun hentEnhet(nyIverksett: Iverksett) =
            familieIntegrasjonerClient.hentNavEnhetForOppfølging(nyIverksett.søker.personIdent)!!

    private fun finnesÅpenTilbakekrevingsbehandling(nyIverksett: Iverksett) =
            tilbakekrevingClient.finnesÅpenBehandling(nyIverksett.fagsak.eksternId)

    private fun Iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat: BeriketSimuleringsresultat) =
            this.copy(
                    vedtak = this.vedtak.copy(
                            tilbakekreving = oppfriskTilbakekreving(this, beriketSimuleringsresultat)
                    )
            )

    private fun oppfriskTilbakekreving(iverksett: Iverksett,
                                       beriketSimuleringsresultat: BeriketSimuleringsresultat): Tilbakekrevingsdetaljer? {

        val tilbakekreving = iverksett.vedtak.tilbakekreving
        val varsel = tilbakekreving?.tilbakekrevingMedVarsel
        val simuleringsoppsummering = beriketSimuleringsresultat.oppsummering

        if (tilbakekreving == null && simuleringsoppsummering.feilutbetaling > BigDecimal.ZERO)
            return tilbakekrevingUtenVarsel()
        else if (varsel != null && simuleringsoppsummering.feilutbetaling != varsel.sumFeilutbetaling)
            return tilbakekreving.oppdaterVarsel(simuleringsoppsummering)
        else
            return tilbakekreving
    }

    private fun Iverksett.skalTilbakekreves() =
            this.vedtak.tilbakekreving!= null &&
            this.vedtak.tilbakekreving.tilbakekrevingsvalg != Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING


    private fun tilbakekrevingUtenVarsel() =
            Tilbakekrevingsdetaljer(Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,null)

    private fun Tilbakekrevingsdetaljer.oppdaterVarsel(simuleringsoppsummering: Simuleringsoppsummering) =
            this.copy(
                    tilbakekrevingMedVarsel = this.tilbakekrevingMedVarsel?.copy(
                    sumFeilutbetaling = simuleringsoppsummering.feilutbetaling,
                    perioder = simuleringsoppsummering.perioder.map { Periode(it.fom, it.tom) }))

    companion object {
        const val TYPE = "opprettTilbakekrevingsbehandling"
    }
}


