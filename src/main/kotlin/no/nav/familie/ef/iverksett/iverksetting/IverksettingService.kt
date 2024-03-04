package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.task.hovedflyt
import no.nav.familie.ef.iverksett.infrastruktur.task.publiseringsflyt
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingsOppgaveForOvergangsstønadTask
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkTask
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Properties
import java.util.UUID

@Service
class IverksettingService(
    val taskService: TaskService,
    val oppdragClient: OppdragClient,
    val iverksettingRepository: IverksettingRepository,
    val iverksettResultatService: IverksettResultatService,
    val featureToggleService: FeatureToggleService,
) {
    @Transactional
    fun startIverksetting(
        iverksett: IverksettData,
        brev: Brev?,
    ) {
        if (featureToggleService.isEnabled("familie.ef.iverksett.stopp-iverksetting")) {
            error("Iverksetting er skrudd av - kan derfor ikke iverksette akkurat nå")
        }
        iverksettingRepository.insert(
            Iverksett(
                iverksett.behandling.behandlingId,
                iverksett,
                iverksett.behandling.eksternId,
                brev,
            ),
        )

        iverksettResultatService.opprettTomtResultat(iverksett.behandling.behandlingId)

        taskService.save(
            Task(
                type = førsteHovedflytTask(iverksett),
                payload = iverksett.behandling.behandlingId.toString(),
                properties =
                    Properties().apply {
                        this["personIdent"] = iverksett.søker.personIdent
                        this["behandlingId"] = iverksett.behandling.behandlingId.toString()
                        this["saksbehandler"] = iverksett.vedtak.saksbehandlerId
                        this["beslutter"] = iverksett.vedtak.beslutterId
                    },
            ),
        )
    }

    @Transactional
    fun publiserVedtak(behandlingId: UUID) {
        val iverksettDbo = iverksettingRepository.findByIdOrThrow(behandlingId)

        taskService.save(
            Task(
                type = førstePubliseringsflytTask(iverksettDbo.data),
                payload = behandlingId.toString(),
                properties =
                    Properties().apply {
                        this["personIdent"] = iverksettDbo.data.søker.personIdent
                        this["behandlingId"] = behandlingId.toString()
                        this["saksbehandler"] = iverksettDbo.data.vedtak.saksbehandlerId
                        this["beslutter"] = iverksettDbo.data.vedtak.beslutterId
                    },
            ),
        )
    }

    private fun førstePubliseringsflytTask(iverksett: IverksettData) =
        when {
            iverksett.erGOmregning() || iverksett.erSatsendring() -> VedtakstatistikkTask.TYPE
            erIverksettingUtenVedtaksperioder(iverksett) -> OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE
            else -> publiseringsflyt().first().type
        }

    private fun førsteHovedflytTask(iverksett: IverksettData) =
        when {
            erIverksettingUtenVedtaksperioder(iverksett) -> JournalførVedtaksbrevTask.TYPE
            else -> hovedflyt().first().type
        }

    private fun erIverksettingUtenVedtaksperioder(iverksett: IverksettData) =
        iverksett.vedtak.tilkjentYtelse == null && iverksett.vedtak.vedtaksresultat == Vedtaksresultat.AVSLÅTT

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = iverksettResultatService.hentIverksettResultat(behandlingId)
        return iverksettResultat?.let {
            if (it.vedtaksbrevResultat.isNotEmpty()) {
                return IverksettStatus.OK
            }
            if (it.journalpostResultat.isNotEmpty()) {
                return IverksettStatus.JOURNALFØRT
            }
            it.oppdragResultat?.let { oppdragResultat ->
                return when (oppdragResultat.oppdragStatus) {
                    OppdragStatus.KVITTERT_OK -> IverksettStatus.OK_MOT_OPPDRAG
                    OppdragStatus.LAGT_PÅ_KØ -> IverksettStatus.SENDT_TIL_OPPDRAG
                    else -> IverksettStatus.FEILET_MOT_OPPDRAG
                }
            }
            it.tilkjentYtelseForUtbetaling?.let {
                if (it.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty() == true) {
                    return IverksettStatus.OK_MOT_OPPDRAG
                }
                return IverksettStatus.SENDT_TIL_OPPDRAG
            }
            return IverksettStatus.IKKE_PÅBEGYNT
        }
    }

    fun sjekkStatusPåIverksettOgOppdaterTilstand(
        stønadstype: StønadType,
        personIdent: String,
        eksternBehandlingId: Long,
        behandlingId: UUID,
    ) {
        val oppdragId =
            OppdragId(
                fagsystem = stønadstype.tilKlassifisering(),
                personIdent = personIdent,
                behandlingsId = eksternBehandlingId.toString(),
            )

        val (status, melding) = oppdragClient.hentStatus(oppdragId)

        if (status != OppdragStatus.KVITTERT_OK) {
            throw TaskExceptionUtenStackTrace("Status fra oppdrag er ikke ok, status=$status melding=$melding")
        }

        iverksettResultatService.oppdaterOppdragResultat(
            behandlingId = behandlingId,
            OppdragResultat(oppdragStatus = status),
        )
    }
}
