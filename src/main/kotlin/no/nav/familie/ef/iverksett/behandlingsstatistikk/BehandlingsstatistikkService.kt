package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.sak.featuretoggle.FeatureToggleService
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class BehandlingsstatistikkService(private val behandlingsstatistikkProducer: BehandlingsstatistikkProducer,
                                   private val behandlingsstatistikkRepository: BehandlingsstatistikkRepository,
                                   private val featureToggleService: FeatureToggleService) {

    @Transactional
    fun lagreBehandlingstatistikk(behandlingstatistikk: BehandlingsstatistikkDto) {
        if (!featureToggleService.isEnabled("familie.ef.iverksett.send-behandlingsstatistikk")) {
            throw IllegalStateException("Sender ikke behandlingsstatistikk pga avskrudd feature toggle")
        }
        val behandlingDVH = mapTilBehandlingDVH(behandlingstatistikk)
        behandlingsstatistikkRepository.lagre(behandlingstatistikk.behandlingId,
                                              behandlingDVH,
                                              behandlingstatistikk.hendelse)
        behandlingsstatistikkProducer.sendBehandling(behandlingDVH)
    }

    private fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingsstatistikkDto): BehandlingDVH {

        val tekniskTid = ZonedDateTime.now(ZoneId.of("UTC")) // De andre datoene blir på UTC-format når de kommer fra ef-sak - så da gjør vi det samme for disse
        return when (behandlingstatistikk.hendelse) {
            Hendelse.MOTTATT -> {
                BehandlingDVH(behandlingId = behandlingstatistikk.behandlingId.toString(),
                              sakId = behandlingstatistikk.eksternFagsakId,
                              personIdent = behandlingstatistikk.personIdent,
                              registrertTid = behandlingstatistikk.hendelseTidspunkt,
                              endretTid = behandlingstatistikk.hendelseTidspunkt,
                              tekniskTid = tekniskTid,
                              behandlingStatus = Hendelse.MOTTATT.name,
                              opprettetAv = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                  behandlingstatistikk.gjeldendeSaksbehandlerId),
                              saksnummer = behandlingstatistikk.eksternFagsakId,
                              mottattTid = behandlingstatistikk.søknadstidspunkt,
                              saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                    behandlingstatistikk.gjeldendeSaksbehandlerId),
                              opprettetEnhet = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                     behandlingstatistikk.opprettetEnhet),
                              ansvarligEnhet = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                     behandlingstatistikk.ansvarligEnhet),
                              behandlingMetode = "MANUELL",
                              avsender = "NAV enslig forelder",
                              behandlingType = behandlingstatistikk.behandlingstype.name,
                              sakYtelse = behandlingstatistikk.stønadstype.name,
                              totrinnsbehandling = true,
                              sakUtland = "Nasjonal"

                )

            }
            Hendelse.PÅBEGYNT -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.MOTTATT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = tekniskTid,
                                   saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                         behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.PÅBEGYNT.name)

            }
            Hendelse.VEDTATT -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.PÅBEGYNT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   vedtakTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = tekniskTid,
                                   saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                         behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.VEDTATT.name,
                                   behandlingResultat = behandlingstatistikk.behandlingResultat,
                                   resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse)


            }
            Hendelse.BESLUTTET -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.VEDTATT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = tekniskTid,
                                   ansvarligBeslutter = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                              behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.BESLUTTET.name,
                                   behandlingResultat = behandlingstatistikk.behandlingResultat,
                                   resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse)
            }
            Hendelse.FERDIG -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.BESLUTTET)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = tekniskTid,
                                   ferdigBehandletTid = behandlingstatistikk.hendelseTidspunkt,
                                   behandlingStatus = Hendelse.FERDIG.name)
            }
        }
    }

    private fun sjekkStrengtFortrolig(erStrengtFortrolig: Boolean, verdi: String): String {
        if (erStrengtFortrolig) {
            return "-5"
        }
        return verdi
    }

}
