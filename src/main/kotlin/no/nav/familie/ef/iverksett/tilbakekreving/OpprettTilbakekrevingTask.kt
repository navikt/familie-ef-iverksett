package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.infrastruktur.task.opprettNesteTask
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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
        val iverksett = iverksettingRepository.hent(behandlingId)

        // TODO: hent beriket simulering og se om feilutbetaling er endret eller har oppstått
        // simuleringService.

        if (iverksett.vedtak.tilbakekreving != null) {
            val enhet = familieIntegrasjonerClient.hentNavEnhetForOppfølging(iverksett.søker.personIdent)!!


            val opprettTilbakekrevingRequest = TilbakekrevingMapper.map(iverksett, enhet)

            tilbakekrevingClient.opprettBehandling(opprettTilbakekrevingRequest)

            tilstandRepository.oppdaterTilbakekrevingResultat(behandlingId = behandlingId,
                                                              TilbakekrevingResultat(opprettTilbakekrevingRequest)
            )

            logger.info("Opprettet tilbakekrevingsbehandling for behandling=[${behandlingId}]")
        } else {
            logger.info("Tilbakekreving ikke funnet for behandling=[${behandlingId}]")
        }
    }

    companion object {
        const val TYPE = "opprettTilbakekrevingsbehandling"
    }
}