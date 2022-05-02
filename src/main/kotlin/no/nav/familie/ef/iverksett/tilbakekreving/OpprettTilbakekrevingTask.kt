package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.tilSimulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.ef.iverksett.økonomi.simulering.harFeilutbetaling
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
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
                                private val taskRepository: TaskRepository,
                                private val tilbakekrevingClient: TilbakekrevingClient,
                                private val tilstandRepository: TilstandRepository,
                                private val simuleringService: SimuleringService,
                                private val familieIntegrasjonerClient: FamilieIntegrasjonerClient) : AsyncTaskStep {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.hent(behandlingId)
        val nyIverksett = hentOppdatertIverksettHvisDetSkalLagesTilbakekreving(iverksett, behandlingId)
        if(nyIverksett!= null) {
            opprettTilbakekreving(behandlingId, nyIverksett)
        }

    }

    private fun hentOppdatertIverksettHvisDetSkalLagesTilbakekreving(iverksett: Iverksett,
                                                                     behandlingId: UUID?): Iverksett? {
        if (!iverksett.vedtak.tilbakekreving.skalTilbakekreves) {
            logger.info("Tilbakekreving ikke valgt for behandlingId=$behandlingId. Oppretter ikke tilbakekrevingsbehandling.")
            return null
        } else if (iverksett.vedtak.tilkjentYtelse == null) {
            logger.warn("OpprettTilbakekrevingTask ikke utført - tilkjentYtelse er null, Behandling: $behandlingId")
            return null
        } else if (iverksett.behandling.behandlingType == BehandlingType.FØRSTEGANGSBEHANDLING) {
            logger.error("Førstegangsbehandling trenger ikke tilbakekreving behandlingId=$behandlingId")
            return null
        } else if (finnesÅpenTilbakekrevingsbehandling(iverksett)) {
            logger.info("Det finnnes allerede tilbakekrevingsbehandling for behandling=${behandlingId}")
            return null
        }

        val nyBeriketSimuleringsresultat = hentBeriketSimulering(iverksett)
        val nyIverksett = iverksett.oppfriskTilbakekreving(nyBeriketSimuleringsresultat)

        loggForskjell(nyIverksett, iverksett, behandlingId)

        if (!nyBeriketSimuleringsresultat.harFeilutbetaling()) {
            logger.info("Behandling=${behandlingId} har ikke (lenger) positiv feilutbetaling i simuleringen")
            return null
        }

        return nyIverksett
    }

    private fun opprettTilbakekreving(behandlingId: UUID,
                                      nyIverksett: Iverksett) {
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

    private fun loggForskjell(nyIverksett: Iverksett,
                              iverksett: Iverksett,
                              behandlingId: UUID?) {
        if (nyIverksett != iverksett) {
            logger.info("Grunnlaget for tilbakekreving for behandling=${behandlingId} har endret seg siden saksbehandlingen")
        }
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(task.opprettNesteTask())
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


