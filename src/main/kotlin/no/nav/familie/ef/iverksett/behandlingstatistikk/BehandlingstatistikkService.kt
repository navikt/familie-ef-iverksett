package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class BehandlingstatistikkService {

    fun sendBehandlingstatistikk(behandlingstatistikk: BehandlingStatistikkDto) {
        //if ikke finnes fra før -> mapping og lagre ned
        // else hente fra lager, oppdater og lagre ned
        //send til DVH
    }

    fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto): BehandlingDVH {
        return when (behandlingstatistikk.hendelse) {
            Hendelse.MOTTATT -> opprettBehandlingDVH(behandlingstatistikk)
            else -> error("Hendelse mangler implementasjon")
        }
    }

    fun opprettBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto): BehandlingDVH {
        return BehandlingDVH(behandlingId = behandlingstatistikk.behandlingId.toString(),
                             aktorId = behandlingstatistikk.personIdent,
                             saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                             registrertTid = behandlingstatistikk.hendelseTidspunkt,
                             endretTid = behandlingstatistikk.hendelseTidspunkt,
                             tekniskTid = ZonedDateTime.now(), //mulig skal flyttes
                             sakYtelse = "EFOG",
                             behandlingType = "Førstegangsbehandling",
                             behandlingStatus = behandlingstatistikk.hendelse.toString(),
                             opprettetAv = behandlingstatistikk.gjeldendeSaksbehandlerId,
                             opprettetEnhet = "", //finne ut
                             ansvarligEnhet = "", //finne ut
                             saksnummer = behandlingstatistikk.saksnummer,
                             mottattTid = behandlingstatistikk.hendelseTidspunkt,
                             behandlingResultat = behandlingstatistikk.behandlingResultat,
                             resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse,
                             behandlingMetode = "MANUELL",
                             avsender = "NAV Enslig forelder") //Er dette riktig?

    }
}