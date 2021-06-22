package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class BehandlingsstatistikkService(private val behandlingsstatistikkProducer: BehandlingsstatistikkProducer,
                                   private val behandlingsstatistikkRepository: BehandlingsstatistikkRepository) {

    @Transactional
    fun lagreBehandlingstatistikk(behandlingstatistikk: BehandlingsstatistikkDto) {
        val behandlingDVH = mapTilBehandlingDVH(behandlingstatistikk)
        behandlingsstatistikkRepository.lagre(behandlingstatistikk.behandlingId,
                                              behandlingDVH,
                                              behandlingstatistikk.hendelse)
        behandlingsstatistikkProducer.sendBehandling(behandlingDVH)
    }

    private fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingsstatistikkDto): BehandlingDVH {

        return when (behandlingstatistikk.hendelse) {
            Hendelse.MOTTATT -> {
                BehandlingDVH(behandlingId = behandlingstatistikk.behandlingId.toString(),
                              sakId = behandlingstatistikk.eksternFagsakId,
                              personIdent = behandlingstatistikk.personIdent,
                              registrertTid = behandlingstatistikk.hendelseTidspunkt,
                              endretTid = behandlingstatistikk.hendelseTidspunkt,
                              tekniskTid = ZonedDateTime.now(),
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
                              sakYtelse = behandlingstatistikk.stønadstype.tilKlassifisering()
                )

            }
            Hendelse.PÅBEGYNT -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.MOTTATT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = ZonedDateTime.now(),
                                   saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                         behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.PÅBEGYNT.name)

            }
            Hendelse.VEDTATT -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.PÅBEGYNT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   vedtakTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = ZonedDateTime.now(),
                                   saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                         behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.VEDTATT.name,
                                   behandlingResultat = behandlingstatistikk.behandlingResultat,
                                   resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse)


            }
            Hendelse.BESLUTTET -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.VEDTATT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = ZonedDateTime.now(),
                                   ansvarligBeslutter = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                              behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.BESLUTTET.name,
                                   behandlingResultat = behandlingstatistikk.behandlingResultat,
                                   resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse)
            }
            Hendelse.FERDIG -> {
                val behandlingDVH = behandlingsstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.BESLUTTET)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = ZonedDateTime.now(),
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
