package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.infrastruktur.task.hovedflyt
import no.nav.familie.ef.iverksett.infrastruktur.task.publiseringsflyt
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingsOppgaveTask
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkTask
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Properties
import java.util.UUID

@Service
class IverksettingService(val taskRepository: TaskRepository,
                          val oppdragClient: OppdragClient,
                          val iverksettingRepository: IverksettingRepository,
                          val tilstandRepository: TilstandRepository) {

    @Transactional
    fun startIverksetting(iverksett: Iverksett, brev: Brev?) {

        iverksettingRepository.lagre(
                iverksett.behandling.behandlingId,
                iverksett,
                brev
        )

        tilstandRepository.opprettTomtResultat(iverksett.behandling.behandlingId)

        taskRepository.save(Task(type = førsteHovedflytTask(iverksett),
                                 payload = iverksett.behandling.behandlingId.toString(),
                                 properties = Properties().apply {
                                     this["personIdent"] = iverksett.søker.personIdent
                                     this["behandlingId"] = iverksett.behandling.behandlingId.toString()
                                     this["saksbehandler"] = iverksett.vedtak.saksbehandlerId
                                     this["beslutter"] = iverksett.vedtak.beslutterId
                                 })
        )
    }

    @Transactional
    fun publiserVedtak(behandlingId: UUID) {
        val iverksett = iverksettingRepository.hent(behandlingId)

        taskRepository.save(Task(
                type = førstePubliseringsflytTask(iverksett),
                payload = behandlingId.toString(),
                properties = Properties().apply {
                    this["personIdent"] = iverksett.søker.personIdent
                    this["behandlingId"] = behandlingId.toString()
                    this["saksbehandler"] = iverksett.vedtak.saksbehandlerId
                    this["beslutter"] = iverksett.vedtak.beslutterId
                }
        ))
    }

    private fun førstePubliseringsflytTask(iverksett: Iverksett) = when {
        erIverksettingUtenVedtaksperioder(iverksett) -> OpprettOppfølgingsOppgaveTask.TYPE
        else -> publiseringsflyt().first().type
    }

    private fun førsteHovedflytTask(iverksett: Iverksett) = when {
        erIverksettingUtenVedtaksperioder(iverksett) -> JournalførVedtaksbrevTask.TYPE
        else -> hovedflyt().first().type
    }

    private fun erIverksettingUtenVedtaksperioder(iverksett: Iverksett) =
            iverksett.vedtak.tilkjentYtelse == null && iverksett.vedtak.vedtaksresultat == Vedtaksresultat.AVSLÅTT

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = tilstandRepository.hentIverksettResultat(behandlingId)
        return iverksettResultat?.let {
            it.vedtaksbrevResultat?.let {
                return IverksettStatus.OK
            }
            it.journalpostResultat?.let {
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
                return IverksettStatus.SENDT_TIL_OPPDRAG
            }
            return IverksettStatus.IKKE_PÅBEGYNT
        }
    }

    fun sjekkStatusPåIverksettOgOppdaterTilstand(stønadstype: StønadType,
                                                 personIdent: String,
                                                 eksternBehandlingId: Long,
                                                 behandlingId: UUID) {
        val oppdragId = OppdragId(
                fagsystem = stønadstype.tilKlassifisering(),
                personIdent = personIdent,
                behandlingsId = eksternBehandlingId.toString()
        )

        val (status, melding) = oppdragClient.hentStatus(oppdragId)

        if (status != OppdragStatus.KVITTERT_OK) {
            throw TaskExceptionUtenStackTrace("Status fra oppdrag er ikke ok, status=$status melding=$melding")
        }

        tilstandRepository.oppdaterOppdragResultat(
                behandlingId = behandlingId,
                OppdragResultat(oppdragStatus = status)
        )
    }
}