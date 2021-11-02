package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.tilSimulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.ef.iverksett.økonomi.simulering.harFeilutbetaling
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

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
        val iverksett = iverksettingRepository.hent(behandlingId)

        if (iverksett.vedtak.tilkjentYtelse == null) {
            logger.warn("OpprettTilbakekrevingTask ikke utført - tilkjentYtelse er null, Behandling: $behandlingId")
            return
        }
        val beriketSimuleringsresultat = hentBeriketSimulering(iverksett)
        val nyIverksett = iverksett.oppfriskTilbakekreving(beriketSimuleringsresultat)

        if (nyIverksett != iverksett) {
            logger.info("Grunnlaget for tilbakekreving for behandling=${behandlingId} har endret seg siden saksbehandlingen")
        }

        if (!nyIverksett.vedtak.tilbakekreving.skalTilbakekreves) {
            logger.debug("Behandling=${behandlingId} skal ikke tilbakekreves")
        } else if (!beriketSimuleringsresultat.harFeilutbetaling()) {
            logger.info("Behandling=${behandlingId} har ikke (lenger) positiv feilutbetaling i simuleringen")
        } else if (finnesÅpenTilbakekrevingsbehandling(nyIverksett)) {
            logger.info("Det finnnes allerede tilbakekrevingsbehandling for behandling=${behandlingId}")
        } else {
            logger.info("Det kreves tilbakekrevingsbehandling for behandling=${behandlingId}")
            val opprettTilbakekrevingRequest = lagTilbakekrevingRequest(nyIverksett)
            tilbakekrevingClient.opprettBehandling(opprettTilbakekrevingRequest)
            tilstandRepository.oppdaterTilbakekrevingResultat(
                    behandlingId = behandlingId,
                    TilbakekrevingResultat(opprettTilbakekrevingRequest))

            // Burde iverksett oppdateres i DBen, siden tilbakekreving potensielt er endret?
            // iverksettingRepository.lagreIverksett(behandlingId,nyIverksett)
            logger.info("Opprettet tilbakekrevingsbehandling for behandling=${behandlingId}")
        }
    }

    private fun hentBeriketSimulering(originalIverksett: Iverksett): BeriketSimuleringsresultat {
        val simuleringRequest = originalIverksett.tilSimulering()
        return simuleringService.hentBeriketSimulering(simuleringRequest)
    }

    private fun lagTilbakekrevingRequest(iverksett: Iverksett): OpprettTilbakekrevingRequest {
        // Henter ut på nytt, selv om noe finnes i iverksett-dto'en
        val enhet = familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(iverksett.søker.personIdent)!!
        return iverksett.tilOpprettTilbakekrevingRequest(enhet)
    }

    private fun finnesÅpenTilbakekrevingsbehandling(nyIverksett: Iverksett): Boolean =
            tilbakekrevingClient.finnesÅpenBehandling(nyIverksett.fagsak.eksternId)

    companion object {

        const val TYPE = "opprettTilbakekrevingsbehandling"
    }
}


