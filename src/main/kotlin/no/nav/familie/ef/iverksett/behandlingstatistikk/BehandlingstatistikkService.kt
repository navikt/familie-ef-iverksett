package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class BehandlingstatistikkService(private val behandlingstatistikkRepository: BehandlingstatistikkRepository) {

    fun lagreBehandlingstatistikk(behandlingstatistikk: BehandlingStatistikkDto) {
        val behandlingDVH = mapTilBehandlingDVH(behandlingstatistikk)
        behandlingstatistikkRepository.lagre(behandlingstatistikk.behandlingId,
                                             behandlingDVH,
                                             behandlingstatistikk.hendelse)

    }

    private fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto): BehandlingDVH {

        return when (behandlingstatistikk.hendelse) {
            Hendelse.MOTTATT -> {
                BehandlingDVH(behandlingId = behandlingstatistikk.behandlingId.toString(),
                              sakId = behandlingstatistikk.saksnummer,
                              aktorId = behandlingstatistikk.personIdent,
                              registrertTid = behandlingstatistikk.hendelseTidspunkt,
                              endretTid = behandlingstatistikk.hendelseTidspunkt,
                              tekniskTid = ZonedDateTime.now(),
                              behandlingStatus = Hendelse.MOTTATT.name,
                              opprettetAv = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                  behandlingstatistikk.gjeldendeSaksbehandlerId),
                              saksnummer = behandlingstatistikk.saksnummer,
                              mottattTid = behandlingstatistikk.hendelseTidspunkt,
                              saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                              opprettetEnhet = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                     behandlingstatistikk.opprettetEnhet),
                              ansvarligEnhet = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                     behandlingstatistikk.ansvarligEnhet),
                              behandlingMetode = "MANUELL",
                              avsender = "NAV enslig forelder",
                              behandlingType = behandlingstatistikk.behandlingstype.name,
                              sakYtelse = behandlingstatistikk.sakYtelse.tilKlassifisering()
                )

            }
            Hendelse.PÅBEGYNT -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.MOTTATT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = ZonedDateTime.now(),
                                   saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                         behandlingstatistikk.gjeldendeSaksbehandlerId),
                                   behandlingStatus = Hendelse.PÅBEGYNT.name)

            }
            Hendelse.VEDTATT -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.PÅBEGYNT)
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
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.VEDTATT)
                behandlingDVH.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                   tekniskTid = ZonedDateTime.now(),
                                   ansvarligBeslutter = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                   behandlingStatus = Hendelse.BESLUTTET.name,
                                   behandlingResultat = behandlingstatistikk.behandlingResultat,
                                   resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse)
            }
            Hendelse.FERDIG -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.VEDTATT)
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

    private fun Stønadstype.tilKlassifisering() = when (this) {
        Stønadstype.OVERGANGSSTØNAD -> "EFOG"
        Stønadstype.BARNETILSYN -> "EFBT"
        Stønadstype.SKOLEPENGER -> "EFSP"
    }

}
